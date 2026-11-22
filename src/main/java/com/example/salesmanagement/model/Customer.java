package com.example.salesmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customers")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;
    @NotBlank @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String phone;
    @Size(max = 500)
    @Column(length = 500)
    private String address;

    public Customer() { }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
