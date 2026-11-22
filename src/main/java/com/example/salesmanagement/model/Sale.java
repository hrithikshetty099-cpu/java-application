package com.example.salesmanagement.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
public class Sale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "invoice_number", nullable = false, unique = true, length = 40)
    private String invoiceNumber;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal cgst;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal sgst;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal igst;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    protected Sale() { }
    public Sale(String invoiceNumber, Customer customer, LocalDateTime saleDate,
                BigDecimal subtotal, BigDecimal cgst, BigDecimal sgst, BigDecimal igst, BigDecimal total) {
        this.invoiceNumber = invoiceNumber; this.customer = customer; this.saleDate = saleDate;
        this.subtotal = subtotal; this.cgst = cgst; this.sgst = sgst; this.igst = igst; this.total = total;
    }
    public void setTotals(BigDecimal subtotal, BigDecimal cgst, BigDecimal sgst, BigDecimal igst, BigDecimal total) {
        this.subtotal = subtotal; this.cgst = cgst; this.sgst = sgst; this.igst = igst; this.total = total;
    }
    public void addItem(SaleItem item) { items.add(item); item.setSale(this); }
    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Customer getCustomer() { return customer; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getCgst() { return cgst; }
    public BigDecimal getSgst() { return sgst; }
    public BigDecimal getIgst() { return igst; }
    public BigDecimal getTotal() { return total; }
    public List<SaleItem> getItems() { return items; }
}
