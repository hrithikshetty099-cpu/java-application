# Daily Sales Management and GST Billing System

A Java 17, Spring Boot, Thymeleaf and MySQL application for managing products, customers, stock, GST invoices and sales reports.

## Requirements

- JDK 17
- Maven 3.8+
- MySQL 8+
- VS Code with the Extension Pack for Java (recommended)

## 1. Configure MySQL

Start MySQL and create the application database and a dedicated account (or use an existing local development account):

```sql
CREATE DATABASE daily_sales_gst CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sales_app'@'localhost' IDENTIFIED BY 'Choose-A-Strong-Password';
GRANT ALL PRIVILEGES ON daily_sales_gst.* TO 'sales_app'@'localhost';
FLUSH PRIVILEGES;
```

The default JDBC URL points to `localhost:3306/daily_sales_gst`. Hibernate creates/updates the tables on application startup from the JPA entities. The relational SQL reference is in `src/main/resources/schema.sql`.

Configure credentials in PowerShell before starting the application (these values last only for the current terminal):

```powershell
$env:DB_USERNAME = "sales_app"
$env:DB_PASSWORD = "Choose-A-Strong-Password"
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "Use-A-Unique-Admin-Password"
mvn spring-boot:run
```

Alternatively, update `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` in the VS Code launch configuration or environment. Do not commit real credentials. On the first startup, an admin user is created using `ADMIN_USERNAME` and `ADMIN_PASSWORD`; the password is stored as a BCrypt hash. The default credentials (`admin` / `ChangeMe123!`) are for local evaluation only—override them before use. Changing these variables later does not reset an already-created admin account.

The first startup also inserts a few sample products and customers when their respective tables are empty.

## 2. Run in VS Code

1. Open this folder in VS Code and install **Extension Pack for Java**.
2. Ensure VS Code uses JDK 17 (`Java: Configure Java Runtime`).
3. Configure the MySQL environment variables above in the integrated terminal.
4. Run `mvn spring-boot:run`, or open `SalesManagementApplication.java` and choose **Run**.
5. Visit [http://localhost:8080](http://localhost:8080) and sign in with the configured admin credentials.

Build and run the focused billing tests:

```powershell
mvn test
mvn package
```

## Features and manual acceptance checklist

- **Authentication:** `/login`, BCrypt password storage, Spring Security protected pages, CSRF-protected forms, and logout from the sidebar.
- **Dashboard:** today's/month-to-date/year-to-date sales, product and customer counts, recent invoices, low-stock alerts and a seven-day sales chart.
- **Products:** add/edit/delete, SKU uniqueness, price/GST validation, available stock and per-product low-stock threshold. Products attached to invoices cannot be deleted.
- **Customers:** add/edit/delete, contact details, and customer-specific purchase history. Customers attached to invoices cannot be deleted.
- **GST billing:** `/billing`; add one or more products, choose customer, payment method and intrastate/interstate tax treatment. Verify CGST + SGST or IGST totals, invoice number, payment record and stock reduction.
- **Inventory safety:** product rows are locked during the billing transaction; insufficient stock fails the invoice and payment together.
- **Invoices:** search by invoice number or customer, view and print a formatted invoice, or download a PDF.
- **Reports:** daily, weekly, monthly, yearly and date-range views, with Excel (`.xlsx`) and PDF downloads.

Suggested end-to-end verification:

1. Sign in; verify the dashboard and low-stock panel.
2. Create a product with a known price, GST rate and stock; edit it and verify it appears in the catalog.
3. Create a customer, then open **Create bill** and add two products.
4. Generate an intrastate bill. Check subtotal, CGST, SGST, final total, invoice and payment; confirm stock decreased.
5. Generate an interstate bill and check IGST replaces CGST/SGST.
6. Attempt to bill more than available stock. The error should be shown and no invoice/payment should be created.
7. Search the invoice by number and customer, view/print/download it, and confirm the customer's history lists it.
8. Select a report period/date range and download/open both export formats.
9. Sign out and confirm that protected URLs redirect to sign-in.

## Database tables

The JPA model maps `admin`, `products`, `customers`, `sales`, `sale_items` and `payments`, with foreign keys from invoices to customers and line items/payments to invoices. Product stock and all invoice monetary values use validated integer quantities and `BigDecimal` columns.
