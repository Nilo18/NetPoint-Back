package com.netpoint.main.services;

import com.netpoint.main.dto.requests.*;
import com.netpoint.main.models.*;
import com.netpoint.main.repositories.*;
import com.netpoint.main.repositories.ProductAttributeRepository;
import com.netpoint.main.repositories.ProductRepository;
import com.netpoint.main.repositories.ProductAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import com.netpoint.main.dto.*;



@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final CompanyRepository companyRepository;



    @Transactional
    public ProductAttributeDTO createAttribute(Integer companyId, CreateProductAttributeRequest request) {
        if (productAttributeRepository.existsByAttributeNameAndCompanyId(request.getAttributeName(), companyId)) {
            throw new RuntimeException("Attribute with this name already exists");
        }

        Company company = companyRepository.findById(Long.valueOf(companyId))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        ProductAttribute attribute = new ProductAttribute();
        attribute.setAttributeName(request.getAttributeName());
        attribute.setAttributeType(request.getAttributeType());
        attribute.setCompany(company);

        ProductAttribute saved = productAttributeRepository.save(attribute);
        return mapToAttributeDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeDTO> getCompanyAttributes(Integer companyId) {
        return productAttributeRepository.findByCompanyId(companyId)
                .stream()
                .map(this::mapToAttributeDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAttribute(Integer companyId, Integer attributeId) {
        // ormagad amowmebs rom es atributi am kompaniisa
        ProductAttribute attribute = productAttributeRepository.findByIdAndCompanyId(attributeId, companyId)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));


        productAttributeValueRepository.deleteByAttributeId(attributeId);

        productAttributeRepository.delete(attribute);
    }


    @Transactional
    public ProductDTO createProduct(Integer companyId, CreateProductRequest request) {
        Company company = companyRepository.findById(Long.valueOf(companyId))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCompany(company);

        Product savedProduct = productRepository.save(product);

        if (request.getCustomAttributes() != null && !request.getCustomAttributes().isEmpty()) {
            saveCustomAttributes(savedProduct, companyId, request.getCustomAttributes());
        }

        return mapToProductDTO(savedProduct);
    }


    @Transactional(readOnly = true)
    public List<ProductDTO> getCompanyProducts(Integer companyId) {
        return productRepository.findByCompanyId(companyId)
                .stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer companyId, Integer productId) {
        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToProductDTO(product);
    }

    @Transactional
    public ProductDTO updateProduct(Integer companyId, Integer productId, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        Product updated = productRepository.save(product);

        // davapdeitet custom attributebi ro moetana
        if (request.getCustomAttributes() != null) {
            // wavashot dzvel values da davamato axlebi
            productAttributeValueRepository.deleteByProductId(productId);
            saveCustomAttributes(updated, companyId, request.getCustomAttributes());
        }

        return mapToProductDTO(updated);
    }

    @Transactional
    public void deleteProduct(Integer companyId, Integer productId) {
        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }



    private void saveCustomAttributes(Product product, Integer companyId, Map<String, String> attributes) {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            Integer attributeId = Integer.parseInt(entry.getKey());

            // shevamowmot attribute arsebobs da es companiis attributes
            ProductAttribute attribute = productAttributeRepository.findByIdAndCompanyId(attributeId, companyId)
                    .orElseThrow(() -> new RuntimeException("Attribute not found: " + attributeId));

            // validacias vuketebt values sachiroebisamebr
            validateAttributeValue(attribute, entry.getValue());

            ProductAttributeValue value = new ProductAttributeValue();
            value.setProduct(product);
            value.setAttribute(attribute);
            value.setValue(entry.getValue());

            productAttributeValueRepository.save(value);
        }
    }

    private void validateAttributeValue(ProductAttribute attribute, String value) {
        switch (attribute.getAttributeType()) {
            case NUMBER:
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid number format for attribute: " + attribute.getAttributeName());
                }
                break;
            case BOOLEAN:
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new RuntimeException("Invalid boolean value for attribute: " + attribute.getAttributeName());
                }
                break;
            case TEXT:

                break;
        }
    }

    private ProductDTO mapToProductDTO(Product product) {
        Map<String, String> customAttrs = new HashMap<>();


        List<ProductAttributeValue> values = productAttributeValueRepository.findByProductId(product.getId());

        if (values != null) {
            for (ProductAttributeValue value : values) {
                if (value.getAttribute() != null) {
                    customAttrs.put(value.getAttribute().getAttributeName(), value.getValue());
                }
            }
        }

        return new ProductDTO(product.getId(), product.getName(), product.getPrice(), customAttrs);
    }
    private ProductAttributeDTO mapToAttributeDTO(ProductAttribute attribute) {
        return new ProductAttributeDTO(attribute.getId(), attribute.getAttributeName(), attribute.getAttributeType());
    }
}