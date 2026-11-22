package com.example.salesmanagement.model;

import com.example.salesmanagement.repository.*;
import com.example.salesmanagement.service.BillingService;
import com.example.salesmanagement.web.BillingForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillingServiceTest {
    private SaleRepository sales;
    private ProductRepository products;
    private CustomerRepository customers;
    private PaymentRepository payments;
    private BillingService billing;
    private Product product;
    private Customer customer;

    @BeforeEach
    void setUp() {
        sales = mock(SaleRepository.class);
        products = mock(ProductRepository.class);
        customers = mock(CustomerRepository.class);
        payments = mock(PaymentRepository.class);
        billing = new BillingService(sales, products, customers, payments);
        product = new Product();
        product.setName("Notebook"); product.setSku("NB-1");
        product.setPrice(new BigDecimal("100.00")); product.setGstRate(new BigDecimal("18.00"));
        product.setStock(10);
        customer = new Customer(); customer.setName("Test customer");
        customer.setPhone("9000000000");
        when(customers.findById(1L)).thenReturn(Optional.of(customer));
        when(products.findByIdForUpdate(2L)).thenReturn(Optional.of(product));
        when(sales.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsIntrastateInvoiceAndDecrementsStock() {
        BillingForm form = form(2);
        Sale sale = billing.create(form);

        assertEquals(new BigDecimal("200.00"), sale.getSubtotal());
        assertEquals(new BigDecimal("18.00"), sale.getCgst());
        assertEquals(new BigDecimal("18.00"), sale.getSgst());
        assertEquals(new BigDecimal("0.00"), sale.getIgst());
        assertEquals(new BigDecimal("236.00"), sale.getTotal());
        assertEquals(8, product.getStock());
        assertEquals(1, sale.getItems().size());
        verify(payments).save(any(Payment.class));
    }

    @Test
    void createsInterstateInvoiceUsingIgst() {
        BillingForm form = form(1);
        form.setInterstate(true);
        Sale sale = billing.create(form);

        assertEquals(new BigDecimal("18.00"), sale.getIgst());
        assertEquals(new BigDecimal("0.00"), sale.getCgst());
        assertEquals(new BigDecimal("0.00"), sale.getSgst());
        assertEquals(new BigDecimal("118.00"), sale.getTotal());
    }

    @Test
    void rejectsInsufficientStockWithoutCreatingSaleOrPayment() {
        product.setStock(1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> billing.create(form(2)));

        assertTrue(error.getMessage().contains("Insufficient stock"));
        assertEquals(1, product.getStock());
        verify(sales, never()).save(any(Sale.class));
        verify(payments, never()).save(any(Payment.class));
    }

    @Test
    void checksCumulativeQuantityWhenProductAppearsOnMultipleLines() {
        product.setStock(3);
        BillingForm form = form(2);
        BillingForm.Line second = new BillingForm.Line();
        second.setProductId(2L);
        second.setQuantity(2);
        form.setLines(java.util.List.of(form.getLines().get(0), second));

        assertThrows(IllegalArgumentException.class, () -> billing.create(form));

        assertEquals(3, product.getStock());
        verify(sales, never()).save(any(Sale.class));
        verify(payments, never()).save(any(Payment.class));
    }

    private BillingForm form(int quantity) {
        BillingForm form = new BillingForm();
        form.setCustomerId(1L);
        BillingForm.Line line = new BillingForm.Line();
        line.setProductId(2L); line.setQuantity(quantity);
        form.setLines(java.util.List.of(line));
        return form;
    }
}
