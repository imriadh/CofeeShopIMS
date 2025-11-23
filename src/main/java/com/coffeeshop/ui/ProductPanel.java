package com.coffeeshop.ui;

import com.coffeeshop.dao.ProductDAO;
import com.coffeeshop.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ProductPanel extends JPanel {
    private ProductDAO productDAO;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, categoryField, priceField, stockField;
    private DataUpdateListener dataUpdateListener;

    // Track the ID of the product currently being edited
    private int selectedProductId = -1;

    public ProductPanel() {
        productDAO = new ProductDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initInputPanel();
        initTablePanel();
        loadProducts();
    }

    public void setDataUpdateListener(DataUpdateListener listener) {
        this.dataUpdateListener = listener;
    }

    public void refreshData() {
        loadProducts();
    }

    private void initInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Manage Product"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField(15);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);

        // Category
        gbc.gridx = 2;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Category:"), gbc);
        categoryField = new JTextField(10);
        gbc.gridx = 3;
        inputPanel.add(categoryField, gbc);

        // Price
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Price:"), gbc);
        priceField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(priceField, gbc);

        // Stock
        gbc.gridx = 2;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Stock:"), gbc);
        stockField = new JTextField(10);
        gbc.gridx = 3;
        inputPanel.add(stockField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        clearButton.addActionListener(e -> clearFields());

        btnPanel.add(addButton);
        btnPanel.add(updateButton);
        btnPanel.add(deleteButton);
        btnPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        inputPanel.add(btnPanel, gbc);

        add(inputPanel, BorderLayout.NORTH);
    }

    private void initTablePanel() {
        String[] columns = { "ID", "Name", "Category", "Price", "Stock" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);

        // Add mouse listener to populate fields on click
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = productTable.getSelectedRow();
                if (row != -1) {
                    selectedProductId = (int) tableModel.getValueAt(row, 0);
                    nameField.setText((String) tableModel.getValueAt(row, 1));
                    categoryField.setText((String) tableModel.getValueAt(row, 2));
                    priceField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                    stockField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
                }
            }
        });

        add(new JScrollPane(productTable), BorderLayout.CENTER);
    }

    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[] {
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getStockQuantity()
            });
        }
    }

    private void addProduct() {
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name required");
                return;
            }

            Product p = new Product(0, name, category, price, stock);
            productDAO.addProduct(p);
            loadProducts();
            clearFields();

            if (dataUpdateListener != null) {
                dataUpdateListener.onDataUpdated();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format");
        }
    }

    private void updateProduct() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Select a product to update");
            return;
        }

        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name required");
                return;
            }

            Product p = new Product(selectedProductId, name, category, price, stock);
            productDAO.updateProduct(p);
            loadProducts();
            clearFields();
            JOptionPane.showMessageDialog(this, "Product updated successfully");

            if (dataUpdateListener != null) {
                dataUpdateListener.onDataUpdated();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format");
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a product to delete");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        productDAO.deleteProduct(id);
        loadProducts();
        clearFields();

        if (dataUpdateListener != null) {
            dataUpdateListener.onDataUpdated();
        }
    }

    private void clearFields() {
        selectedProductId = -1;
        nameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        stockField.setText("");
        productTable.clearSelection();
    }
}
