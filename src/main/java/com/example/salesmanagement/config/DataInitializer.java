package com.example.salesmanagement.config;

import com.example.salesmanagement.model.*;
import com.example.salesmanagement.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initializeData(AdminRepository admins, ProductRepository products,
                                     CustomerRepository customers, PasswordEncoder encoder,
                                     @Value("${app.admin.username}") String username,
                                     @Value("${app.admin.password}") String password) {
        return args -> {
            if (admins.count() == 0) admins.save(new Admin(username, encoder.encode(password), "ADMIN"));
            if (products.count() == 0) {
                products.save(product("Everyday Notebook", "ST-1001", "85.00", "12.00", 48));
                products.save(product("Ballpoint Pen Set", "ST-1002", "120.00", "18.00", 7));
                products.save(product("Desk Organizer", "ST-1003", "450.00", "18.00", 3));
            }
            if (customers.count() == 0) {
                Customer customer = new Customer();
                customer.setName("Aarav Sharma"); customer.setPhone("9876543210");
                customer.setAddress("12 Market Road, Pune");
                customers.save(customer);
                Customer second = new Customer();
                second.setName("Mira Patel"); second.setPhone("9123456780");
                second.setAddress("8 Lake View, Mumbai");
                customers.save(second);
            }
        };
    }

    private Product product(String name, String sku, String price, String gst, int stock) {
        Product product = new Product();
        product.setName(name); product.setSku(sku); product.setPrice(new BigDecimal(price));
        product.setGstRate(new BigDecimal(gst)); product.setStock(stock); product.setLowStockThreshold(5);
        return product;
    }
}
