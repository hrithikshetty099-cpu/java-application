package com.example.salesmanagement.service;

import com.example.salesmanagement.model.*;
import com.example.salesmanagement.repository.*;
import com.example.salesmanagement.web.BillingForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BillingService {
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private final SaleRepository sales;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final PaymentRepository payments;

    public BillingService(SaleRepository sales, ProductRepository products,
                          CustomerRepository customers, PaymentRepository payments) {
        this.sales = sales; this.products = products; this.customers = customers; this.payments = payments;
    }

    @Transactional
    public Sale create(BillingForm form) {
        Customer customer = customers.findById(form.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Selected customer no longer exists."));
        if (!StringUtils.hasText(form.getPaymentMethod()) ||
                !java.util.Set.of("CASH", "CARD", "UPI", "BANK_TRANSFER").contains(form.getPaymentMethod())) {
            throw new IllegalArgumentException("Choose a valid payment method.");
        }
        BigDecimal subtotal = new BigDecimal("0.00"),
                cgst = new BigDecimal("0.00"),
                sgst = new BigDecimal("0.00"),
                igst = new BigDecimal("0.00");
        Sale sale = new Sale(invoiceNumber(), customer, LocalDateTime.now(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Map<Long, Product> lockedProducts = new HashMap<>();
        Map<Long, Integer> quantitiesByProduct = new HashMap<>();

        for (BillingForm.Line line : form.getLines()) {
            if (line.getProductId() == null || line.getQuantity() == null || line.getQuantity() < 1) {
                throw new IllegalArgumentException("Every invoice line needs a product and a quantity of at least one.");
            }
            Product product = products.findByIdForUpdate(line.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("A selected product no longer exists."));
            int requested = quantitiesByProduct.getOrDefault(product.getId(), 0) + line.getQuantity();
            if (product.getStock() < requested) {
                throw new IllegalArgumentException("Insufficient stock for " + product.getName() +
                        " (available: " + (product.getStock() - quantitiesByProduct.getOrDefault(product.getId(), 0)) + ").");
            }
            lockedProducts.put(product.getId(), product);
            quantitiesByProduct.put(product.getId(), requested);
            BigDecimal lineSubtotal = product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTax = lineSubtotal.multiply(product.getGstRate())
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSubtotal);
            if (form.isInterstate()) {
                igst = igst.add(lineTax);
            } else {
                BigDecimal lineCgst = lineTax.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                cgst = cgst.add(lineCgst);
                sgst = sgst.add(lineTax.subtract(lineCgst));
            }
            sale.addItem(new SaleItem(product, line.getQuantity(), product.getPrice(), product.getGstRate(), lineSubtotal));
        }

        quantitiesByProduct.forEach((productId, quantity) ->
                lockedProducts.get(productId).setStock(lockedProducts.get(productId).getStock() - quantity));
        BigDecimal total = subtotal.add(cgst).add(sgst).add(igst).setScale(2, RoundingMode.HALF_UP);
        sale.setTotals(subtotal, cgst, sgst, igst, total);
        sale = sales.save(sale);
        payments.save(new Payment(sale, total, form.getPaymentMethod(), LocalDateTime.now()));
        return sale;
    }

    private String invoiceNumber() {
        return "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
