# Daily Sales Management and GST Billing System

A Java 17, Spring Boot, Thymeleaf and Supabase PostgreSQL application for managing products, customers, stock, GST invoices and sales reports.

## Requirements

- JDK 17
- Maven 3.8+
- A Supabase project with its PostgreSQL database enabled
- VS Code with the Extension Pack for Java (recommended)

## 1. Configure Supabase PostgreSQL

In the Supabase dashboard, open **Project Settings → Database** and obtain the PostgreSQL connection details. Use the direct connection or the Supavisor session pooler; use the host, port, database name, and username Supabase provides. The connection URL must use the PostgreSQL JDBC format and require SSL, for example:

```text
jdbc:postgresql://<supabase-host>:5432/postgres?sslmode=require
```

Hibernate creates or updates the application's tables on startup from the existing JPA entities. The PostgreSQL relational SQL reference is in `src/main/resources/schema.sql`. `ddl-auto=update` is non-destructive, but it does not migrate records from a previous MySQL database; back up and migrate any existing records separately before switching production data sources.

Set the connection values as environment variables in PowerShell before starting the application (these values last only for the current terminal). Do not commit credentials or post them in chat:

```powershell
$env:DB_URL = "jdbc:postgresql://<supabase-host>:5432/postgres?sslmode=require"
$env:DB_USERNAME = "<supabase-database-username>"
$env:DB_PASSWORD = "<supabase-database-password>"
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "<choose-a-unique-admin-password>"
```

The application requires all three database variables; it has no local database or password fallback. PostgreSQL TLS is required by the datasource configuration. On the first startup, an admin user is created using `ADMIN_USERNAME` and `ADMIN_PASSWORD`; the password is stored as a BCrypt hash. Changing these variables later does not reset an already-created admin account.

The first startup also inserts a few sample products and customers when their respective tables are empty.

## 2. Build and run

From the project directory, in the same PowerShell terminal where the environment variables were set:

```powershell
mvn clean install
mvn spring-boot:run
```

Maven runs the tests as part of `clean install`. Successful application startup confirms it connected to the configured database. If startup fails, check the exception for a missing environment variable, invalid Supabase host/port/database/user/password, or network access restrictions in Supabase. Never disable SSL to work around a connection error.

Visit [http://localhost:8080](http://localhost:8080) and sign in with the configured admin credentials. In VS Code, select JDK 17 using **Java: Configure Java Runtime** before running the same Maven commands in the integrated terminal.

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
