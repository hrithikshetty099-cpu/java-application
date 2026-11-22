-- Hibernate manages the schema on startup (spring.jpa.hibernate.ddl-auto=update).
-- This file documents the relational structure for MySQL deployments.
CREATE TABLE IF NOT EXISTS admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(80) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  role VARCHAR(30) NOT NULL
);
CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  sku VARCHAR(60) NOT NULL UNIQUE,
  price DECIMAL(12,2) NOT NULL,
  gst_rate DECIMAL(5,2) NOT NULL,
  stock INT NOT NULL,
  low_stock_threshold INT NOT NULL DEFAULT 5
);
CREATE TABLE IF NOT EXISTS customers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  address VARCHAR(500)
);
CREATE TABLE IF NOT EXISTS sales (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  invoice_number VARCHAR(40) NOT NULL UNIQUE,
  customer_id BIGINT NOT NULL,
  sale_date DATETIME NOT NULL,
  subtotal DECIMAL(14,2) NOT NULL,
  cgst DECIMAL(14,2) NOT NULL,
  sgst DECIMAL(14,2) NOT NULL,
  igst DECIMAL(14,2) NOT NULL,
  total DECIMAL(14,2) NOT NULL,
  CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);
CREATE TABLE IF NOT EXISTS sale_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sale_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(150) NOT NULL,
  sku VARCHAR(60) NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  gst_rate DECIMAL(5,2) NOT NULL,
  line_subtotal DECIMAL(14,2) NOT NULL,
  CONSTRAINT fk_item_sale FOREIGN KEY (sale_id) REFERENCES sales(id),
  CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(id)
);
CREATE TABLE IF NOT EXISTS payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sale_id BIGINT NOT NULL,
  amount DECIMAL(14,2) NOT NULL,
  payment_method VARCHAR(30) NOT NULL,
  payment_date DATETIME NOT NULL,
  CONSTRAINT fk_payment_sale FOREIGN KEY (sale_id) REFERENCES sales(id)
);
