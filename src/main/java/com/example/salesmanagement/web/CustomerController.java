package com.example.salesmanagement.web;

import com.example.salesmanagement.model.Customer;
import com.example.salesmanagement.repository.CustomerRepository;
import com.example.salesmanagement.repository.SaleRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerRepository customers;
    private final SaleRepository sales;
    public CustomerController(CustomerRepository customers, SaleRepository sales) {
        this.customers = customers; this.sales = sales;
    }
    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customers.findAllByOrderByNameAsc());
        return "customers";
    }
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("customer", new Customer()); model.addAttribute("pageTitle", "Add customer");
        return "customer-form";
    }
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customers.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND)));
        model.addAttribute("pageTitle", "Edit customer");
        return "customer-form";
    }
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Customer customer, BindingResult result, Model model,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", customer.getId() == null ? "Add customer" : "Edit customer");
            return "customer-form";
        }
        customers.save(customer);
        redirect.addFlashAttribute("success", "Customer saved successfully.");
        return "redirect:/customers";
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            customers.deleteById(id);
            redirect.addFlashAttribute("success", "Customer deleted.");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "This customer has invoices and cannot be deleted.");
        }
        return "redirect:/customers";
    }
    @GetMapping("/{id}/history")
    public String history(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customers.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND)));
        model.addAttribute("purchases", sales.findByCustomerIdOrderBySaleDateDesc(id));
        return "customer-history";
    }
}
