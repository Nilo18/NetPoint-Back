package com.netpoint.main.controllers;

import com.netpoint.main.dto.AuthenticatedUser;
import com.netpoint.main.dto.requests.*;
import com.netpoint.main.dto.responses.*;
import com.netpoint.main.dto.ProductAttributeDTO;
import com.netpoint.main.dto.ProductDTO;
import com.netpoint.main.services.PlanEnforcementService;
import com.netpoint.main.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Log
public class ProductController {

    private final ProductService productService;
    private final PlanEnforcementService planEnforcementService;


    @PostMapping("/attributes")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ProductAttributeDTO> createAttribute(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateProductAttributeRequest request) {

        ProductAttributeDTO attribute = productService.createAttribute(user.companyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(attribute);
    }

    @GetMapping("/attributes")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<List<ProductAttributeDTO>> getAttributes(@AuthenticationPrincipal AuthenticatedUser user) {
        List<ProductAttributeDTO> attributes = productService.getCompanyAttributes(user.companyId());
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/artificial-attributes")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    public ResponseEntity<List<ProductAttributeDTO>> getArtificialAttributes(@AuthenticationPrincipal
                                                                             AuthenticatedUser user) {
        return ResponseEntity.ok(productService.getArtificialProductAttributes(user.companyId()));
    }

    @PutMapping("/attributes")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ProductAttributeDTO> updateAttribute(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProductAttributeDTO request) {
        ProductAttributeDTO attribute = productService.updateAttribute(user.companyId(), request);
        return ResponseEntity.ok(attribute);
    }

    @DeleteMapping("/attributes/{id}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    public ResponseEntity<ProductAttributeDTO> deleteAttribute(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        // tipebs cvlis imat rasac databaza elis
        Integer numericCompanyId = user.companyId();


        return ResponseEntity.ok(productService.deleteAttribute(numericCompanyId, id));

        // abrunebs HTTP 204 No Content status/
//        return ResponseEntity.noContent().build();
    }


    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateProductRequest request) {
        planEnforcementService.enforceProductLimit(user.companyId());
        // iwers dabrunebul ProductDTOs servisidan
        ProductDTO product = productService.createProduct(user.companyId(), request);

        //products awvdis null is magivrad
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String sortBy,
            @RequestParam(defaultValue = "") String sortDirection,
            @RequestParam(defaultValue = "") String filterOption,
            @RequestParam(defaultValue = "") String filterFrom,
            @RequestParam(defaultValue = "") String filterTo,
            @AuthenticationPrincipal AuthenticatedUser user) {
        List<ProductDTO> products = productService.getCompanyProducts(user.companyId());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ProductDTO> getProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer productId) {
        ProductDTO product = productService.getProductById(user.companyId(), productId);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDTO product = productService.updateProduct(user.companyId(), productId, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    public ResponseEntity<GenericResponse> deleteProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer productId) {
        return ResponseEntity.ok(productService.deleteProduct(user.companyId(), productId));
    }
}
