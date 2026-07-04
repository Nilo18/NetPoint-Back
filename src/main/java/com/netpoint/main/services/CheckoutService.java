package com.netpoint.main.services;

import com.netpoint.main.dto.SaleItemDTO;
import com.netpoint.main.dto.requests.CheckoutRequest;
import com.netpoint.main.dto.requests.CheckoutRequestItem;
import com.netpoint.main.dto.responses.SaleResponse;
import com.netpoint.main.exceptions.BadRequestException;
import com.netpoint.main.exceptions.ProductNotFoundException;
import com.netpoint.main.models.*;
import com.netpoint.main.repositories.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Data
@Service
public class CheckoutService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;


    @Transactional
    public SaleResponse checkout(Integer companyId, Integer userId, CheckoutRequest request) {
//        if (request.items().isEmpty()) {
//            throw new BadRequestException("The cart is empty");
//        }

        log.debug("CHECKOUT METHOD IS RUNNING");
        User user = userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new BadRequestException("User does not belong to this company"));

        List<Integer> productIds = new ArrayList<>();

        request.items().forEach(item -> {
            productIds.add(item.productId());
        });

        List<Product> products = productRepository.findByIdInAndCompany_Id(productIds, companyId)
                .orElseThrow(() -> new BadRequestException("Not all products were found for this company"));

        boolean everyProductBelongsToCompany = products.stream().allMatch(
                product -> product.getCompany().getId().equals(companyId)
        );

        if (!everyProductBelongsToCompany) {
            throw new BadRequestException("Not all products belong to the given company");
        }

        boolean everyProductHasValidQuantity = request.items().stream().allMatch(
                item -> item.quantity() > 0);

        if (!everyProductHasValidQuantity) {
            throw new BadRequestException("Not all products have valid quantity");
        }

        boolean allProductsHaveEnoughStock =  products.stream().allMatch(
                product -> product.getStock() > 0
        );

        if (!allProductsHaveEnoughStock) {
            throw new BadRequestException("Not all products have enough stock");
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        Map<Integer, Product> productsById = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<SaleItem> saleItems = new ArrayList<>();

        for (CheckoutRequestItem item : request.items()) {
            Product product = productsById.get(item.productId());

            if (product == null) {
                throw new ProductNotFoundException("Product not found");
            }

            BigDecimal unitRetailPrice = product.getPrice();
            BigDecimal unitWholesalePrice = product.getWholesalePrice();
            BigDecimal quantity = BigDecimal.valueOf(item.quantity());

            BigDecimal lineRevenue = unitRetailPrice.multiply(quantity);
            BigDecimal lineCost = unitWholesalePrice.multiply(quantity);
            BigDecimal lineProfit = lineRevenue.subtract(lineCost);

            SaleItem saleItem = new SaleItem();
            saleItem.setProduct(product);
            saleItem.setProductNameSnapshot(product.getName());
            saleItem.setQuantity(item.quantity());
            saleItem.setUnitRetailPrice(unitRetailPrice);
            saleItem.setUnitWholesalePrice(unitWholesalePrice);
            saleItem.setLineRevenue(lineRevenue);
            saleItem.setLineCost(lineCost);
            saleItem.setLineProfit(lineProfit);

            saleItems.add(saleItem);

            totalRevenue = totalRevenue.add(lineRevenue);
            totalCost = totalCost.add(lineCost);
            totalProfit = totalProfit.add(lineProfit);

            product.setStock(product.getStock() - item.quantity());
        }

        Sale sale = new Sale();
        sale.setCompany(user.getCompany());
        sale.setUser(user);
        sale.setCashierNameSnapshot(user.getName());
        sale.setItems(saleItems);
        sale.setTotalRevenue(totalRevenue);
        sale.setTotalCost(totalCost);
        sale.setTotalProfit(totalProfit);
        LocalDateTime current = LocalDateTime.now();
        sale.setCreatedAt(current);

        for (SaleItem saleItem : saleItems) {
            saleItem.setSale(sale);
        }

        log.debug("POSSIBLE LINE BEFORE ERROR");
        saleRepository.save(sale);
        log.debug("THIS SHOULD NOT BE PRINTED");

        log.debug("ATTEMPTING TO SAVE AUDIT LOG...");
        auditLogService.log(user.getCompany(), user, AuditLog.EventType.SALE_COMPLETED,
                "Sale completed: " + saleItems.size() + " item(s), total " + totalRevenue);
        log.debug("SAVED AUDIT LOG...");

        List<SaleItemDTO> restrictedSaleItems = saleItems.stream().map(
                item -> new SaleItemDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitRetailPrice(),
                        item.getLineRevenue()
                )
        ).toList();

        return new SaleResponse(
            user.getName(),
            totalRevenue,
                current,
            restrictedSaleItems
        );
    }
}
