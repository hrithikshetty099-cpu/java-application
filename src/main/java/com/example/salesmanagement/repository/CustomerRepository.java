package com.example.salesmanagement.repository;

import com.example.salesmanagement.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findAllByOrderByNameAsc();
}
