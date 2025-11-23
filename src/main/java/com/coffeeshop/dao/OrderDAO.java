package com.coffeeshop.dao;

import com.coffeeshop.config.DatabaseConfig;
import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderItem;
import java.sql.*;

public class OrderDAO {

    public boolean saveOrder(Order order) {
        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement stockStmt = null;
        ResultSet generatedKeys = null;

        String insertOrderSQL = "INSERT INTO orders (customer_name, total_amount, user_id, payment_method, card_number) VALUES (?, ?, ?, ?, ?)";
        String insertItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, subtotal) VALUES (?, ?, ?, ?, ?)";
        String updateStockSQL = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert Order
            orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, order.getCustomerName());
            orderStmt.setDouble(2, order.getTotalAmount());
            orderStmt.setInt(3, order.getUserId());
            orderStmt.setString(4, order.getPaymentMethod());
            orderStmt.setString(5, order.getCardNumber());

            int affectedRows = orderStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            generatedKeys = orderStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                order.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // 2. Insert Items and Update Stock
            itemStmt = conn.prepareStatement(insertItemSQL);
            stockStmt = conn.prepareStatement(updateStockSQL);

            for (OrderItem item : order.getItems()) {
                // Add Item
                itemStmt.setInt(1, order.getId());
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getPriceAtPurchase());
                itemStmt.setDouble(5, item.getSubtotal());
                itemStmt.addBatch();

                // Update Stock
                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch();
            stockStmt.executeBatch();

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (generatedKeys != null)
                    generatedKeys.close();
                if (orderStmt != null)
                    orderStmt.close();
                if (itemStmt != null)
                    itemStmt.close();
                if (stockStmt != null)
                    stockStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public java.util.List<Order> getOrdersForReport() {
        java.util.List<Order> orders = new java.util.ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("order_id"));
                order.setCustomerName(rs.getString("customer_name"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                order.setUserId(rs.getInt("user_id"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setCardNumber(rs.getString("card_number"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
}
