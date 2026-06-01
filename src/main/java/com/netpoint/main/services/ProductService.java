package com.netpoint.main.services;

import com.netpoint.main.dto.requests.*;
import com.netpoint.main.exceptions.AttributeAlreadyExistsException;
import com.netpoint.main.exceptions.AttributeCapacityReachedException;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.models.*;
import com.netpoint.main.repositories.*;
import com.netpoint.main.repositories.ProductAttributeRepository;
import com.netpoint.main.repositories.ProductRepository;
import com.netpoint.main.repositories.ProductAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import com.netpoint.main.dto.*;

import javax.management.Attribute;


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

        long numberOfAttributes = productAttributeRepository.countByCompanyId(companyId);

        log.info("Counted numberOfAttributes as: " + numberOfAttributes);
        if (numberOfAttributes >= 10L) {
            throw new AttributeCapacityReachedException("Product can only have 10 attributes");
        }

        Company company = companyRepository.findById(Long.valueOf(companyId))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        ProductAttribute attribute = new ProductAttribute();
        attribute.setAttributeName(request.getAttributeName());
        attribute.setAttributeType(request.getAttributeType());
        attribute.setCompany(company);

        ProductAttribute saved = productAttributeRepository.save(attribute);
        return mapToAttributeDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeDTO> getCompanyAttributes(Integer companyId) {
        List<ProductAttributeDTO> attributes = productAttributeRepository.findByCompany_Id(companyId)
                .stream()
                .map(this::mapToAttributeDTO)
                .collect(Collectors.toList());

        Product product = productRepository.getById(companyId);
        attributes.addFirst(new ProductAttributeDTO(0, "Name", ProductAttribute.AttributeType.TEXT, true));
        attributes.add(1, new ProductAttributeDTO(1, "Price", ProductAttribute.AttributeType.TEXT, true));

        return attributes;
    }

    @Transactional
    public void deleteAttribute(Integer companyId, Integer attributeId) {
        // ormagad amowmebs rom es atributi am kompaniisa
        ProductAttribute attribute = productAttributeRepository.findByIdAndCompany_Id(attributeId, companyId)
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
        return productRepository.findByCompany_Id(companyId)
                .stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
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
            productAttributeValueRepository.deleteByProduct_Id(productId);
            saveCustomAttributes(updated, companyId, request.getCustomAttributes());
        }

        return mapToProductDTO(updated);
    }

    @Transactional
    public void deleteProduct(Integer companyId, Integer productId) {
        Product product = productRepository.findByIdAndCompany_Id(productId, companyId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }



    private void saveCustomAttributes(Product product, Integer companyId, Map<String, String> attributes) {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            Integer attributeId = Integer.parseInt(entry.getKey());

            // shevamowmot attribute arsebobs da es companiis attributes
            ProductAttribute attribute = productAttributeRepository.findByIdAndCompany_Id(attributeId, companyId)
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


        List<ProductAttributeValue> values = productAttributeValueRepository.findByProduct_Id(product.getId());

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
        return new ProductAttributeDTO(attribute.getId(), attribute.getAttributeName(), attribute.getAttributeType(), attribute.isDefault());
    }
}
