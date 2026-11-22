package com.example.salesmanagement.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
public class SaleItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;
    @Column(nullable = false, length = 60)
    private String sku;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
    @Column(name = "gst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal gstRate;
    @Column(name = "line_subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineSubtotal;

    protected SaleItem() { }
    public SaleItem(Product product, int quantity, BigDecimal unitPrice, BigDecimal gstRate, BigDecimal lineSubtotal) {
        this.product = product; this.productName = product.getName(); this.sku = product.getSku();
        this.quantity = quantity; this.unitPrice = unitPrice; this.gstRate = gstRate; this.lineSubtotal = lineSubtotal;
    }
    void setSale(Sale sale) { this.sale = sale; }
    public String getProductName() { return productName; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getGstRate() { return gstRate; }
    public BigDecimal getLineSubtotal() { return lineSubtotal; }
}
