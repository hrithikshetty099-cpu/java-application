package com.example.salesmanagement.web;

import com.example.salesmanagement.model.Product;
import com.example.salesmanagement.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository products;
    public ProductController(ProductRepository products) { this.products = products; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", products.findAllByOrderByNameAsc());
        model.addAttribute("lowStock", products.findLowStock());
        return "products";
    }
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("pageTitle", "Add product");
        return "product-form";
    }
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", products.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND)));
        model.addAttribute("pageTitle", "Edit product");
        return "product-form";
    }
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Product product, BindingResult result, Model model,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", product.getId() == null ? "Add product" : "Edit product");
            return "product-form";
        }
        try {
            products.save(product);
        } catch (DataIntegrityViolationException ex) {
            result.rejectValue("sku", "duplicate", "That SKU is already in use.");
            model.addAttribute("pageTitle", product.getId() == null ? "Add product" : "Edit product");
            return "product-form";
        }
        redirect.addFlashAttribute("success", "Product saved successfully.");
        return "redirect:/products";
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            products.deleteById(id);
            redirect.addFlashAttribute("success", "Product deleted.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "This product is used on an invoice and cannot be deleted.");
        }
        return "redirect:/products";
    }
}
