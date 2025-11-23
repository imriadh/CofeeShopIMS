package com.coffeeshop.config;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initialize() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT CHECK(role IN ('ADMIN', 'STAFF')) DEFAULT 'STAFF'" +
                ");";

        String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "product_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category TEXT, " +
                "price REAL NOT NULL, " +
                "stock_quantity INTEGER DEFAULT 0" +
                ");";

        String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_name TEXT, " +
                "total_amount REAL, " +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "user_id INTEGER, " +
                "payment_method TEXT, " +
                "card_number TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                ");";

        String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER NOT NULL, " +
                "price_at_purchase REAL NOT NULL, " +
                "subtotal REAL NOT NULL, " +
                "FOREIGN KEY(order_id) REFERENCES orders(order_id), " +
                "FOREIGN KEY(product_id) REFERENCES products(product_id)" +
                ");";

        String insertAdmin = "INSERT OR IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');";
        String insertService = "INSERT OR IGNORE INTO users (username, password, role) VALUES ('service', 'service123', 'STAFF');";

        // Sample products (only if empty)

        String[] sampleProducts = {
                "INSERT INTO products (name, category, price, stock_quantity) VALUES ('Espresso', 'Coffee', 3.50, 100)",
                "INSERT INTO products (name, category, price, stock_quantity) VALUES ('Cappuccino', 'Coffee', 4.50, 100)",
                "INSERT INTO products (name, category, price, stock_quantity) VALUES ('Latte', 'Coffee', 4.75, 100)",
                "INSERT INTO products (name, category, price, stock_quantity) VALUES ('Mocha', 'Coffee', 5.00, 100)",
                "INSERT INTO products (name, category, price, stock_quantity) VALUES ('Croissant', 'Food', 3.00, 50)",
                "INSERT INTO products (name, category, price, stock_quantity) VALUES ('Muffin', 'Food', 2.50, 50)"
        };

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createProductsTable);
            stmt.execute(createOrdersTable);
            stmt.execute(createOrderItemsTable);

            stmt.execute(insertAdmin);
            stmt.execute(insertService);

            // Check if products exist
            if (!conn.createStatement().executeQuery("SELECT * FROM products LIMIT 1").next()) {
                for (String sql : sampleProducts) {
                    stmt.execute(sql);
                }
            }

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
