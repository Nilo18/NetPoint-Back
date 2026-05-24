package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.requests.*;
import com.netpoint.main.dto.responses.*;
import com.netpoint.main.dto.ProductAttributeDTO;
import com.netpoint.main.dto.ProductDTO;
import com.netpoint.main.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @PostMapping("/attributes")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ProductAttributeDTO> createAttribute(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateProductAttributeRequest request) {

        ProductAttributeDTO attribute = productService.createAttribute(user.companyId().intValue(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(attribute);
    }

    @GetMapping("/attributes")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<List<ProductAttributeDTO>> getAttributes(@AuthenticationPrincipal AuthenticatedUser user) {
        List<ProductAttributeDTO> attributes = productService.getCompanyAttributes(user.companyId().intValue());
        return ResponseEntity.ok(attributes);
    }

    @DeleteMapping("/attributes/{id}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        // tipebs cvlis imat rasac databaza elis
        Integer numericCompanyId = user.companyId().intValue();


        productService.deleteAttribute(numericCompanyId, id);

        // abrunebs HTTP 204 No Content status
        return ResponseEntity.noContent().build();
    }


    @PostMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ProductDTO> createProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateProductRequest request) {
        // iwers dabrunebul ProductDTOs servisidan
        ProductDTO product = productService.createProduct(user.companyId().intValue(), request);

        //products awvdis null is magivrad
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<List<ProductDTO>> getAllProducts(@AuthenticationPrincipal AuthenticatedUser user) {
        List<ProductDTO> products = productService.getCompanyProducts(user.companyId().intValue());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ProductDTO> getProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer productId) {
        ProductDTO product = productService.getProductById(user.companyId().intValue(), productId);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ProductDTO> updateProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDTO product = productService.updateProduct(user.companyId().intValue(), productId, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer productId) {
        productService.deleteProduct(user.companyId().intValue(), productId);
        return ResponseEntity.noContent().build();
    }
}