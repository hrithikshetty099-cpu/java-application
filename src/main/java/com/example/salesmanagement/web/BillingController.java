package com.example.salesmanagement.web;

import com.example.salesmanagement.model.Sale;
import com.example.salesmanagement.repository.*;
import com.example.salesmanagement.service.BillingService;
import com.example.salesmanagement.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;

@Controller
public class BillingController {
    private final BillingService billing;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final SaleRepository sales;
    private final DocumentService documents;
    @Value("${app.store.name}") private String storeName;
    @Value("${app.store.gstin:}") private String gstin;

    public BillingController(BillingService billing, ProductRepository products, CustomerRepository customers,
                             SaleRepository sales, DocumentService documents) {
        this.billing = billing; this.products = products; this.customers = customers;
        this.sales = sales; this.documents = documents;
    }
    @GetMapping("/billing")
    public String billingForm(Model model) {
        prepareForm(model, new BillingForm());
        return "billing";
    }
    @PostMapping("/billing")
    public String createBill(@Valid @ModelAttribute BillingForm form, BindingResult result, Model model,
                             RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form);
            return "billing";
        }
        try {
            Sale sale = billing.create(form);
            redirect.addFlashAttribute("success", "Invoice " + sale.getInvoiceNumber() + " created.");
            return "redirect:/invoices/" + sale.getInvoiceNumber();
        } catch (IllegalArgumentException ex) {
            result.reject("billing.error", ex.getMessage());
            prepareForm(model, form);
            return "billing";
        }
    }
    @GetMapping("/invoices")
    public String invoiceList(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("invoices", sales.search(q.trim()));
        model.addAttribute("q", q);
        return "invoices";
    }
    @GetMapping("/invoices/{invoiceNumber}")
    public String invoice(@PathVariable String invoiceNumber, Model model) {
        Sale sale = sales.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("sale", sale);
        model.addAttribute("storeName", storeName);
        model.addAttribute("gstin", gstin);
        return "invoice";
    }
    @GetMapping("/invoices/{invoiceNumber}/pdf")
    public ResponseEntity<byte[]> invoicePdf(@PathVariable String invoiceNumber) throws IOException {
        Sale sale = sales.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + invoiceNumber + ".pdf\"")
                .body(documents.invoicePdf(sale, storeName, gstin));
    }
    private void prepareForm(Model model, BillingForm form) {
        model.addAttribute("billingForm", form);
        model.addAttribute("customers", customers.findAllByOrderByNameAsc());
        model.addAttribute("products", products.findAllByOrderByNameAsc());
    }
}
