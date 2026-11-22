package com.example.salesmanagement.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    protected Payment() { }
    public Payment(Sale sale, BigDecimal amount, String paymentMethod, LocalDateTime paymentDate) {
        this.sale = sale; this.amount = amount; this.paymentMethod = paymentMethod; this.paymentDate = paymentDate;
    }
}
