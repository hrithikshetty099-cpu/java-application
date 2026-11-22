package com.example.salesmanagement.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class BillingForm {
    @NotNull(message = "Select a customer")
    private Long customerId;
    private boolean interstate;
    @NotBlank
    private String paymentMethod = "CASH";
    @NotEmpty(message = "Add at least one product")
    @Valid
    private List<Line> lines = new ArrayList<>(List.of(new Line()));

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public boolean isInterstate() { return interstate; }
    public void setInterstate(boolean interstate) { this.interstate = interstate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }

    public static class Line {
        @NotNull(message = "Select a product")
        private Long productId;
        @NotNull @Min(value = 1, message = "Quantity must be at least one")
        private Integer quantity = 1;
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
