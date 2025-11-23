package com.coffeeshop.ui;

import com.coffeeshop.dao.OrderDAO;
import com.coffeeshop.dao.ProductDAO;
import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderItem;
import com.coffeeshop.model.Product;
import com.coffeeshop.model.User;
import com.coffeeshop.service.PdfService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {
    private User currentUser;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private PdfService pdfService;
    private DataUpdateListener dataUpdateListener;

    private JComboBox<Product> productCombo;
    private JTextField quantityField;
    private JTextField customerField;
    private JTextField cardNumberField;
    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JRadioButton cashRadio, cardRadio;

    private List<OrderItem> currentCart;
    private double subtotalAmount = 0.0;
    private static final double TAX_RATE = 0.10; // 10% Tax

    public OrderPanel(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.pdfService = new PdfService();
        this.currentCart = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initLeftPanel();
        initRightPanel();
    }

    public void setDataUpdateListener(DataUpdateListener listener) {
        this.dataUpdateListener = listener;
    }

    public void refreshData() {
        refreshProductList();
    }

    private void initLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Add Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Customer Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(new JLabel("Customer Name:"), gbc);
        customerField = new JTextField(15);
        gbc.gridx = 1;
        leftPanel.add(customerField, gbc);

        // Product Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        leftPanel.add(new JLabel("Product:"), gbc);
        productCombo = new JComboBox<>();
        refreshProductList();
        gbc.gridx = 1;
        leftPanel.add(productCombo, gbc);

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 2;
        leftPanel.add(new JLabel("Quantity:"), gbc);
        quantityField = new JTextField("1", 5);
        gbc.gridx = 1;
        leftPanel.add(quantityField, gbc);

        // Payment Method
        gbc.gridx = 0;
        gbc.gridy = 3;
        leftPanel.add(new JLabel("Payment:"), gbc);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ButtonGroup group = new ButtonGroup();
        cashRadio = new JRadioButton("Cash", true);
        cardRadio = new JRadioButton("Card");
        group.add(cashRadio);
        group.add(cardRadio);
        radioPanel.add(cashRadio);
        radioPanel.add(cardRadio);
        gbc.gridx = 1;
        leftPanel.add(radioPanel, gbc);

        // Card Number Field (Initially Hidden)
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel cardLabel = new JLabel("Card No:");
        leftPanel.add(cardLabel, gbc);
        JTextField cardField = new JTextField(15);
        gbc.gridx = 1;
        leftPanel.add(cardField, gbc);

        cardLabel.setVisible(false);
        cardField.setVisible(false);

        cashRadio.addActionListener(e -> {
            cardLabel.setVisible(false);
            cardField.setVisible(false);
        });
        cardRadio.addActionListener(e -> {
            cardLabel.setVisible(true);
            cardField.setVisible(true);
        });

        // Store reference for checkout
        this.cardNumberField = cardField;

        // Add Button
        JButton addButton = new JButton("Add to Cart");
        addButton.addActionListener(e -> addToCart());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        leftPanel.add(addButton, gbc);

        add(leftPanel, BorderLayout.WEST);
    }

    private void initRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        // Table
        String[] columns = { "Product", "Qty", "Price", "Subtotal" };
        tableModel = new DefaultTableModel(columns, 0);
        cartTable = new JTable(tableModel);
        rightPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: $0.00 (Inc. Tax)");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JButton checkoutButton = new JButton("Checkout & Print");
        checkoutButton.setBackground(new Color(40, 167, 69));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutButton.addActionListener(e -> checkout());
        bottomPanel.add(checkoutButton, BorderLayout.EAST);

        rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void refreshProductList() {
        productCombo.removeAllItems();
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            productCombo.addItem(p);
        }
    }

    private void addToCart() {
        try {
            Product selectedProduct = (Product) productCombo.getSelectedItem();
            int quantity = Integer.parseInt(quantityField.getText());

            if (selectedProduct == null)
                return;
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive");
                return;
            }
            if (selectedProduct.getStockQuantity() < quantity) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient stock! Available: " + selectedProduct.getStockQuantity());
                return;
            }

            OrderItem item = new OrderItem(
                    selectedProduct.getId(),
                    selectedProduct.getName(),
                    quantity,
                    selectedProduct.getPrice());

            currentCart.add(item);
            tableModel.addRow(new Object[] {
                    item.getProductName(),
                    item.getQuantity(),
                    String.format("$%.2f", item.getPriceAtPurchase()),
                    String.format("$%.2f", item.getSubtotal())
            });

            subtotalAmount += item.getSubtotal();
            updateTotalLabel();

            // Temporarily reduce stock in UI object
            selectedProduct.setStockQuantity(selectedProduct.getStockQuantity() - quantity);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity");
        }
    }

    private void updateTotalLabel() {
        double tax = subtotalAmount * TAX_RATE;
        double total = subtotalAmount + tax;
        totalLabel.setText(String.format("Sub: $%.2f | Tax: $%.2f | Total: $%.2f", subtotalAmount, tax, total));
    }

    private void checkout() {
        if (currentCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }
        String customer = customerField.getText().trim();
        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name");
            return;
        }

        double tax = subtotalAmount * TAX_RATE;
        double total = subtotalAmount + tax;

        Order order = new Order();
        order.setCustomerName(customer);
        order.setTotalAmount(total);
        order.setUserId(currentUser.getId());
        order.setItems(currentCart);

        String paymentMethod = cashRadio.isSelected() ? "Cash" : "Card";
        order.setPaymentMethod(paymentMethod);

        if ("Card".equals(paymentMethod)) {
            String cardNo = cardNumberField.getText().trim();
            if (cardNo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter card number");
                return;
            }
            order.setCardNumber(cardNo);
        } else {
            order.setCardNumber(null);
        }

        if (orderDAO.saveOrder(order)) {
            JOptionPane.showMessageDialog(this, "Order placed successfully!");

            // Notify listener to update stock in other panels
            if (dataUpdateListener != null) {
                dataUpdateListener.onDataUpdated();
            }

            pdfService.generateReceipt(order); // You might want to pass payment method here too
            resetOrder();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to place order. Check database connection.");
        }
    }

    private void resetOrder() {
        currentCart.clear();
        tableModel.setRowCount(0);
        subtotalAmount = 0.0;
        updateTotalLabel();
        customerField.setText("");
        refreshProductList(); // Reload stock from DB
    }
}
