package com.netpoint.main.services;

import com.netpoint.main.dto.requests.*;
import com.netpoint.main.dto.responses.GenericResponse;
import com.netpoint.main.dto.responses.PageResponse;
import com.netpoint.main.exceptions.*;
import com.netpoint.main.filters.DefaultProductAttribute;
import com.netpoint.main.models.*;
import com.netpoint.main.repositories.*;
import com.netpoint.main.repositories.ProductAttributeRepository;
import com.netpoint.main.repositories.ProductRepository;
import com.netpoint.main.repositories.ProductAttributeValueRepository;
import jakarta.persistence.criteria.Expression;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.netpoint.main.dto.*;



@Service
@RequiredArgsConstructor
@Log
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final CompanyRepository companyRepository;



    @Transactional
    public ProductAttributeDTO createAttribute(Integer companyId, CreateProductAttributeRequest request) {
        if (productAttributeRepository.existsByAttributeNameAndCompany_Id(request.getAttributeName(), companyId)) {
            throw new AttributeAlreadyExistsException("Attribute with this name already exists");
        }

        List<ProductAttributeDTO> defaultAttributes = getDefaultProductAttributes();
        int defaultAttributeCount = defaultAttributes.size();
        int artificialAttributeCount = productAttributeRepository.countByCompanyId(companyId);

        if (defaultAttributeCount + artificialAttributeCount >= 10) {
            throw new AttributeCapacityReachedException("You can only have up to 10 attributes");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        ProductAttribute attribute = new ProductAttribute();
        attribute.setAttributeName(request.getAttributeName());
        attribute.setAttributeType(request.getAttributeType());
        attribute.setCompany(company);

        ProductAttribute saved = productAttributeRepository.save(attribute);
        return mapToAttributeDTO(saved);
    }

    private List<ProductAttributeDTO> getDefaultProductAttributes() {
        return Arrays.stream(Product.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(DefaultProductAttribute.class))
                .map(field -> {
                    DefaultProductAttribute annotation = field.getAnnotation(DefaultProductAttribute.class);
                    String name = annotation.name().isBlank() ? field.getName() : annotation.name();

                    return new ProductAttributeDTO(
                            null,
                            name,
                            annotation.type(),
                            true
                    );
                })
                .toList();
    }

    public List<ProductAttributeDTO> getArtificialProductAttributes(Integer companyId) {
        return productAttributeRepository.findByCompany_Id(companyId)
                .stream()
                .map(this::mapToAttributeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeDTO> getCompanyAttributes(Integer companyId) {
        List<ProductAttributeDTO> defaultAttributes = getDefaultProductAttributes();

        List<ProductAttributeDTO> artificialAttributes = getArtificialProductAttributes(companyId);

        return Stream.concat(defaultAttributes.stream(), artificialAttributes.stream()).toList();
    }

    @Transactional
    public ProductAttributeDTO updateAttribute(Integer companyId, ProductAttributeDTO newAttribute) {
        log.info("Looking for attribute with id: " + newAttribute.id() + " and company with id: " + companyId);

        ProductAttribute attribute = productAttributeRepository.findByIdAndCompany_Id(newAttribute.id(), companyId)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found"));

        log.info("Possible line before exception");
        if (productAttributeRepository.existsByAttributeNameAndCompany_IdAndIdNot(
                newAttribute.attributeName(), companyId, newAttribute.id())) {
            throw new AttributeAlreadyExistsException("Attribute with this name already exists");
        }
        log.info("Possible line AFTER exception");
        attribute.setAttributeName(newAttribute.attributeName());
        attribute.setAttributeType(newAttribute.attributeType());

        ProductAttribute saved = productAttributeRepository.save(attribute);
        return mapToAttributeDTO(saved);
    }

    @Transactional
    public ProductAttributeDTO deleteAttribute(Integer companyId, Integer attributeId) {
        // ormagad amowmebs rom es atributi am kompaniisa
        ProductAttribute attribute = productAttributeRepository.findByIdAndCompany_Id(attributeId, companyId)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found"));

        productAttributeValueRepository.deleteByAttributeId(attributeId);

        productAttributeRepository.delete(attribute);

        return new ProductAttributeDTO(
                attribute.getId(), attribute.getAttributeName(),
                attribute.getAttributeType(), attribute.isDefault()
        );
    }


    @Transactional
    public ProductDTO createProduct(Integer companyId, CreateProductRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getRetailPrice());
        product.setCompany(company);
        product.setImageUrl(request.getImageUrl());
        Product savedProduct = productRepository.save(product);

        if (request.getCustomAttributes() != null && !request.getCustomAttributes().isEmpty()) {
            saveCustomAttributes(savedProduct, companyId, request.getCustomAttributes());
        }

        product.setWholesalePrice(request.getWholesalePrice());
        product.setStock(request.getStock() != null ? request.getStock() : 0);

        //gamoitvlis wholesalevePrice
        if (request.getWholesalePrice() != null && product.getPrice() != null) {
            BigDecimal margin = product.getPrice()
                    .subtract(request.getWholesalePrice())
                    .divide(product.getPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            validateMarginPercent(margin);
            product.setMarginPercent(margin);
        }

        productRepository.save(product); // save again with new fields
        return mapToProductDTO(savedProduct);
    }

    private Specification<Product> companyIs(Integer companyId) {
        return (root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId);
    }

    private Specification<Product> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + search.trim().toLowerCase() + "%"
            );
        };
    }

    private Specification<Product> profitabilityRange(
            String fromValue,
            String toValue
    ) {
        BigDecimal from = parseRangeValue(fromValue);
        BigDecimal to = parseRangeValue(toValue);

        if (from.compareTo(to) > 0) {
            throw new BadRequestException("Filter from value cannot exceed to value");
        }

        return (root, query, cb) -> {
            Expression<BigDecimal> profitability = cb.diff(
                    root.<BigDecimal>get("price"),
                    root.<BigDecimal>get("wholesalePrice")
            );

            return cb.between(profitability, from, to);
        };
    }

    private Specification<Product> decimalRange(
            String databaseField,
            String fromValue,
            String toValue
    ) {
        BigDecimal from = parseRangeValue(fromValue);
        BigDecimal to = parseRangeValue(toValue);

        if (from.compareTo(to) > 0) {
            throw new BadRequestException("Filter from value cannot exceed to value");
        }

        return (root, query, cb) ->
                cb.between(root.<BigDecimal>get(databaseField), from, to);
    }

    private Specification<Product> stockRange(String fromValue, String toValue) {
        try {
            int from = Integer.parseInt(fromValue);
            int to = Integer.parseInt(toValue);

            if (from < 0 || to < 0 || from > to) {
                throw new BadRequestException("Invalid stock range");
            }

            return (root, query, cb) ->
                    cb.between(root.<Integer>get("stock"), from, to);

        } catch (NumberFormatException exception) {
            throw new BadRequestException("Stock range must contain whole numbers");
        }
    }

    private BigDecimal parseRangeValue(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new BadRequestException("Filter values must be valid numbers");
        }
    }

    private Specification<Product> toSpecification(String filterBy, String filterFrom, String filterTo) {
        String field = filterBy == null ? "" : filterBy.trim().toLowerCase();
        String fromValue = filterFrom == null ? "" : filterFrom.trim();
        String toValue = filterTo == null ? "" : filterTo.trim();

        if (field.isEmpty()) {
            throw new BadRequestException("Filter field is required");
        }

        if (fromValue.isEmpty() || toValue.isEmpty()) {
            throw new BadRequestException("Filter range requires both from and to values");
        }

        return switch (field) {
            case "stock" -> stockRange(fromValue, toValue);
            case "retailprice" -> decimalRange("price", fromValue, toValue);
            case "margin" -> decimalRange("marginPercent", fromValue, toValue);
            case "wholesaleprice" -> decimalRange("wholesalePrice", fromValue, toValue);
            case "profitability" -> profitabilityRange(fromValue, toValue);
            default -> throw new BadRequestException("Unsupported filter field: " + filterBy);
        };
    }
    private Pageable createPageable(ProductQuery query) {
        String property = switch (query.getSortBy()) {
            case "" -> "id";
            case "stock" -> "stock";
            case "retailPrice" -> "price";
            case "wholesalePrice" -> "wholesalePrice";
            case "margin" -> "marginPercent";
            case "profitability" -> throw new BadRequestException(
                    "Profitability sorting is not implemented yet"
            );
            default -> throw new BadRequestException(
                    "Unsupported sort field: " + query.getSortBy()
            );
        };

        Sort.Direction direction = switch (query.getSortDirection().toLowerCase()) {
            case "", "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new BadRequestException(
                    "sortDirection must be asc or desc"
            );
        };

        return PageRequest.of(
                query.getPage(),
                query.getSize(),
                Sort.by(direction, property)
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getCompanyProducts(Integer companyId, ProductQuery query) {
        Pageable pageable = createPageable(query);

        Specification<Product> specification = Specification
                .where(companyIs(companyId))
                .and(matchesSearch(query.getSearch()));

        if (query.getFilterBy() != null && !query.getFilterBy().isBlank()) {
            specification = specification.and(toSpecification(
                    query.getFilterBy(),
                    query.getFilterFrom(),
                    query.getFilterTo()
            ));
        }

        return productRepository.findAll(specification, pageable)
                .map(this::mapToProductDTO);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer companyId, Integer productId) {
        Product product = productRepository.findByIdAndCompany_Id(productId, companyId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToProductDTO(product);
    }

    @Transactional
    public ProductDTO updateProduct(Integer companyId, Integer productId, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndCompany_Id(productId, companyId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getRetailPrice() != null) {
            product.setPrice(request.getRetailPrice());
        }

        Product updated = productRepository.save(product);

        // davapdeitet custom attributebi ro moetana
        if (request.getCustomAttributes() != null) {
            // wavashot dzvel values da davamato axlebi
            productAttributeValueRepository.deleteByProduct_Id(productId);
            saveCustomAttributes(updated, companyId, request.getCustomAttributes());
        }

        //amowmebs ro nullebi araa, es imistvis davamate ro daapdeitebisas ar gaanulos eseni
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getWholesalePrice() != null) product.setWholesalePrice(request.getWholesalePrice());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getStock() != null) product.setStock(request.getStock());

        //gamoitvlis margins
        if (product.getPrice() != null && product.getWholesalePrice() != null) {
            BigDecimal margin = product.getPrice()
                    .subtract(product.getWholesalePrice())
                    .divide(product.getPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            validateMarginPercent(margin);
            product.setMarginPercent(margin);
        }

        productRepository.save(product);
        return mapToProductDTO(updated);
    }

    @Transactional
    public GenericResponse deleteProduct(Integer companyId, Integer productId) {
        Product product = productRepository.findByIdAndCompany_Id(productId, companyId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        productRepository.delete(product);
        return new GenericResponse(200, "Deleted successfully");
    }



    private void saveCustomAttributes(Product product, Integer companyId, Map<String, JsonNode> attributes) {
        for (Map.Entry<String, JsonNode> entry : attributes.entrySet()) {
            log.info("PARSING entry.getKey() AS INT: " + entry.getKey());
            String attributeName = entry.getKey();

            // shevamowmot attribute arsebobs da es companiis attributes
            ProductAttribute attribute = productAttributeRepository.
                    findByAttributeNameAndCompany_Id(attributeName, companyId).
                    orElseThrow(() -> new AttributeNotFoundException(
                            "Attribute with the given name was not found: " + attributeName
                    ));

            // validacias vuketebt values sachiroebisamebr
            validateAttributeValue(attribute, entry.getValue());

            ProductAttributeValue value = new ProductAttributeValue();
            value.setProduct(product);
            value.setAttribute(attribute);
            value.setValue(entry.getValue().asText());

            productAttributeValueRepository.save(value);
        }
    }


    private void validateMarginPercent(BigDecimal margin) {
        if (margin.abs().compareTo(new BigDecimal("99999.99")) > 0) {
            throw new BadRequestException(
                    "Please check the retail and wholesale prices. The calculated margin is too large."
            );
        }
    }

    private void validateAttributeValue(ProductAttribute attribute, JsonNode value) {
        if (value == null || value.isNull()) {
            throw new RuntimeException("Value is required for attribute: " + attribute.getAttributeName());
        }

        log.info("THE ATTRIBUTE IS: " + attribute);
        log.info("VALUE IS: " + value);
        switch (attribute.getAttributeType()) {
            case NUMBER:
                if (!value.isNumber()) {
                    throw new RuntimeException("Invalid number format for attribute: " + attribute.getAttributeName());
                }
                log.info("PARSED VALUE AS: " + value.asDecimal());
                break;
            case BOOLEAN:
                if (!value.isBoolean()) {
                    throw new RuntimeException("Invalid boolean value for attribute: " + attribute.getAttributeName());
                }
                break;
            case DATE:
                if (!value.isTextual()) {
                    throw new RuntimeException("Invalid date value for attribute: " + attribute.getAttributeName());
                }
                try {
                    LocalDate.parse(value.asText());
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid date value for attribute: " + attribute.getAttributeName());
                }
                break;
            case TEXT:
                if (!value.isTextual()) {
                    throw new RuntimeException("Invalid text value for attribute: " + attribute.getAttributeName());
                }
                break;
        }
    }

    private ProductDTO mapToProductDTO(Product product) {
        Map<String, JsonNode> customAttrs = new HashMap<>();
        String imageUrl = product.getImageUrl();

        List<ProductAttributeValue> values = productAttributeValueRepository.findByProduct_Id(product.getId());

        if (values != null) {
            for (ProductAttributeValue value : values) {
                if (value.getAttribute() != null) {
                    customAttrs.put(value.getAttribute().getAttributeName(), mapAttributeValueToJsonNode(value));
                }
            }
        }

        BigDecimal profitability = null;
        if (product.getPrice() != null && product.getWholesalePrice() != null) {
            profitability = product.getPrice().subtract(product.getWholesalePrice());
        }

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                customAttrs,
                product.getStock(),
                product.getWholesalePrice(),
                product.getMarginPercent(),
                profitability,
                imageUrl
        );
    }

    private JsonNode mapAttributeValueToJsonNode(ProductAttributeValue value) {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        try {
            return switch (value.getAttribute().getAttributeType()) {
                case NUMBER -> nodeFactory.numberNode(new BigDecimal(value.getValue()));
                case BOOLEAN -> nodeFactory.booleanNode(Boolean.parseBoolean(value.getValue()));
                case DATE, TEXT -> nodeFactory.stringNode(value.getValue());
            };
        } catch (NumberFormatException e) {
            return nodeFactory.stringNode(value.getValue());
        }
    }

    private ProductAttributeDTO mapToAttributeDTO(ProductAttribute attribute) {
        return new ProductAttributeDTO(attribute.getId(), attribute.getAttributeName(), attribute.getAttributeType(), attribute.isDefault());
    }
}

