package com.example.salesmanagement.web;

import com.example.salesmanagement.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;

@Controller
public class PageController {
    private final SaleRepository sales;
    private final ProductRepository products;
    private final CustomerRepository customers;
    @Value("${app.store.name}") private String storeName;

    public PageController(SaleRepository sales, ProductRepository products, CustomerRepository customers) {
        this.sales = sales; this.products = products; this.customers = customers;
    }
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("dailySales", sales.totalBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        model.addAttribute("monthlySales", sales.totalBetween(today.withDayOfMonth(1).atStartOfDay(),
                today.withDayOfMonth(1).plusMonths(1).atStartOfDay()));
        model.addAttribute("totalRevenue", sales.totalAll());
        model.addAttribute("productCount", products.count());
        model.addAttribute("customerCount", customers.count());
        model.addAttribute("recentSales", sales.findTop8ByOrderBySaleDateDesc());
        model.addAttribute("lowStock", products.findLowStock());
        var labels = new ArrayList<String>();
        var values = new ArrayList<BigDecimal>();
        for (int day = 6; day >= 0; day--) {
            LocalDate date = today.minusDays(day);
            labels.add(date.getDayOfWeek().toString().substring(0, 3));
            values.add(sales.totalBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay()));
        }
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartValues", values);
        model.addAttribute("storeName", storeName);
        return "dashboard";
    }
}
