package com.coffeeshop.ui;

import com.coffeeshop.dao.OrderDAO;
import com.coffeeshop.model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportsPanel extends JPanel {
    private OrderDAO orderDAO;
    private JLabel totalSalesLabel;
    private JLabel totalCashLabel;
    private JLabel totalCardLabel;
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public ReportsPanel() {
        this.orderDAO = new OrderDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 245));

        initSummaryPanel();
        initOrdersTable();

        refreshData();
    }

    private void initSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setOpaque(false);

        totalSalesLabel = createSummaryCard("Total Sales", new Color(63, 81, 181));
        totalCashLabel = createSummaryCard("Total Cash", new Color(76, 175, 80));
        totalCardLabel = createSummaryCard("Total Card", new Color(255, 152, 0));

        summaryPanel.add(totalSalesLabel);
        summaryPanel.add(totalCashLabel);
        summaryPanel.add(totalCardLabel);

        add(summaryPanel, BorderLayout.NORTH);
    }

    private JLabel createSummaryCard(String title, Color color) {
        JLabel label = new JLabel("<html><center>" + title + "<br>$0.00</center></html>", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(color);
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return label;
    }

    private void initOrdersTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Instant Order Preview"));
        tablePanel.setBackground(Color.WHITE);

        String[] columns = { "Order ID", "Customer", "Date", "Total", "Payment", "Card No" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(25);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        tablePanel.add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshData());
        tablePanel.add(refreshButton, BorderLayout.SOUTH);

        add(tablePanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        List<Order> orders = orderDAO.getOrdersForReport();

        double totalSales = 0;
        double totalCash = 0;
        double totalCard = 0;

        tableModel.setRowCount(0);

        for (Order order : orders) {
            totalSales += order.getTotalAmount();
            if ("Cash".equalsIgnoreCase(order.getPaymentMethod())) {
                totalCash += order.getTotalAmount();
            } else if ("Card".equalsIgnoreCase(order.getPaymentMethod())) {
                totalCard += order.getTotalAmount();
            }

            tableModel.addRow(new Object[] {
                    order.getId(),
                    order.getCustomerName(),
                    order.getOrderDate(),
                    String.format("$%.2f", order.getTotalAmount()),
                    order.getPaymentMethod(),
                    order.getCardNumber() != null ? order.getCardNumber() : "-"
            });
        }

        updateSummaryCard(totalSalesLabel, "Total Sales", totalSales);
        updateSummaryCard(totalCashLabel, "Total Cash", totalCash);
        updateSummaryCard(totalCardLabel, "Total Card", totalCard);
    }

    private void updateSummaryCard(JLabel label, String title, double amount) {
        label.setText("<html><center>" + title + "<br>" + String.format("$%.2f", amount) + "</center></html>");
    }
}
