package com.example.salesmanagement.web;

import com.example.salesmanagement.model.Sale;
import com.example.salesmanagement.repository.SaleRepository;
import com.example.salesmanagement.service.DocumentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {
    private final SaleRepository sales;
    private final DocumentService documents;
    public ReportController(SaleRepository sales, DocumentService documents) {
        this.sales = sales; this.documents = documents;
    }

    @GetMapping
    public String report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                         @RequestParam(defaultValue = "custom") String period, Model model) {
        LocalDate today = LocalDate.now();
        if (!"custom".equals(period) || start == null || end == null) {
            switch (period) {
                case "daily" -> { start = today; end = today; }
                case "weekly" -> { start = today.minusDays(6); end = today; }
                case "monthly" -> { start = today.withDayOfMonth(1); end = today; }
                case "yearly" -> { start = today.withDayOfYear(1); end = today; }
                default -> { start = today.withDayOfMonth(1); end = today; }
            }
        }
        if (end.isBefore(start)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        List<Sale> records = sales.findBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        model.addAttribute("records", records);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("total", records.stream().map(Sale::getTotal).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        model.addAttribute("period", period);
        return "reports";
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) throws IOException {
        List<Sale> records = range(start, end);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sales-report.xlsx\"")
                .body(documents.reportExcel(records));
    }
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) throws IOException {
        List<Sale> records = range(start, end);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sales-report.pdf\"")
                .body(documents.reportPdf(records, "Sales report: " + start + " to " + end));
    }
    private List<Sale> range(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both start and end dates are required.");
        }
        if (end.isBefore(start)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        return sales.findBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }
}
