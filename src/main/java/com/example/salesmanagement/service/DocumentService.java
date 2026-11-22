package com.example.salesmanagement.service;

import com.example.salesmanagement.model.Sale;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DocumentService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public byte[] reportExcel(List<Sale> records) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sales");
            Row header = sheet.createRow(0);
            String[] columns = {"Invoice", "Date", "Customer", "Subtotal", "CGST", "SGST", "IGST", "Total"};
            for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);
            int rowIndex = 1;
            for (Sale sale : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(sale.getInvoiceNumber());
                row.createCell(1).setCellValue(DATE.format(sale.getSaleDate()));
                row.createCell(2).setCellValue(sale.getCustomer().getName());
                money(row, 3, sale.getSubtotal()); money(row, 4, sale.getCgst());
                money(row, 5, sale.getSgst()); money(row, 6, sale.getIgst()); money(row, 7, sale.getTotal());
            }
            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
            workbook.write(output);
            return output.toByteArray();
        }
    }

    public byte[] reportPdf(List<Sale> records, String title) throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        lines.add(title);
        lines.add("Generated " + DATE.format(java.time.LocalDateTime.now()));
        lines.add("");
        lines.add(String.format("%-22s %-18s %-22s %12s", "INVOICE", "DATE", "CUSTOMER", "TOTAL (INR)"));
        lines.add("-------------------------------------------------------------------------------");
        for (Sale sale : records) {
            lines.add(String.format("%-22s %-18s %-22s %12s", sale.getInvoiceNumber(),
                    sale.getSaleDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    sale.getCustomer().getName(), sale.getTotal().toPlainString()));
        }
        return createPdf(lines);
    }

    public byte[] invoicePdf(Sale sale, String storeName, String gstin) throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        lines.add(storeName);
        if (gstin != null && !gstin.isBlank()) lines.add("GSTIN: " + gstin);
        lines.add("TAX INVOICE");
        lines.add("Invoice: " + sale.getInvoiceNumber() + "     Date: " + DATE.format(sale.getSaleDate()));
        lines.add("Bill to: " + sale.getCustomer().getName() + " | " + sale.getCustomer().getPhone());
        if (sale.getCustomer().getAddress() != null) lines.add("Address: " + sale.getCustomer().getAddress());
        lines.add("");
        lines.add(String.format("%-24s %-12s %5s %10s %8s %12s", "ITEM", "SKU", "QTY", "PRICE", "GST%", "AMOUNT"));
        lines.add("-------------------------------------------------------------------------------");
        for (var item : sale.getItems()) {
            lines.add(String.format("%-24s %-12s %5d %10s %8s %12s", item.getProductName(), item.getSku(),
                    item.getQuantity(), item.getUnitPrice().toPlainString(), item.getGstRate().toPlainString(),
                    item.getLineSubtotal().toPlainString()));
        }
        lines.add("");
        lines.add("Subtotal: INR " + sale.getSubtotal().toPlainString());
        if (sale.getIgst().signum() > 0) lines.add("IGST: INR " + sale.getIgst().toPlainString());
        else {
            lines.add("CGST: INR " + sale.getCgst().toPlainString());
            lines.add("SGST: INR " + sale.getSgst().toPlainString());
        }
        lines.add("TOTAL: INR " + sale.getTotal().toPlainString());
        lines.add("Thank you for your business.");
        return createPdf(lines);
    }

    private byte[] createPdf(List<String> lines) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            float y = 790;
            for (String line : lines) {
                if (y < 45) {
                    stream.close();
                    page = new PDPage(PDRectangle.A4); document.addPage(page);
                    stream = new PDPageContentStream(document, page); y = 790;
                }
                stream.beginText();
                stream.setFont(y == 790 && !lines.isEmpty() ? bold : font, 9);
                stream.newLineAtOffset(42, y);
                stream.showText(ascii(line));
                stream.endText();
                y -= 17;
            }
            stream.close();
            document.save(output);
            return output.toByteArray();
        }
    }

    private String ascii(String text) {
        String clean = text.replaceAll("[^\\x20-\\x7E]", "?");
        return clean.length() > 110 ? clean.substring(0, 110) : clean;
    }
    private void money(Row row, int column, BigDecimal value) {
        row.createCell(column).setCellValue(value.doubleValue());
    }
}
