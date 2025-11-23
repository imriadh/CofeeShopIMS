package com.coffeeshop.ui;

import com.coffeeshop.model.User;
import com.coffeeshop.service.BackupService;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {
    private User currentUser;

    public MainFrame(User user) {
        this.currentUser = user;
        initUI();
    }

    private void initUI() {
        setTitle("Coffee Shop IMS - " + currentUser.getRole());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Create Panels
        OrderPanel orderPanel = new OrderPanel(currentUser);
        ProductPanel productPanel = null;

        // Order Panel (POS) - Available to all
        tabbedPane.addTab("New Order", orderPanel);

        // Inventory & Reports - Only for ADMIN
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            productPanel = new ProductPanel();
            tabbedPane.addTab("Inventory Management", productPanel);

            ReportsPanel reportsPanel = new ReportsPanel();
            tabbedPane.addTab("Reports & Analytics", reportsPanel);

            // Refresh reports when order is placed
            ReportsPanel finalReportsPanel = reportsPanel;
            ProductPanel finalProductPanel = productPanel;

            orderPanel.setDataUpdateListener(() -> {
                if (finalProductPanel != null)
                    finalProductPanel.refreshData();
                finalReportsPanel.refreshData();
            });

            // When Product is added/deleted -> Refresh Order Panel (Dropdown)
            productPanel.setDataUpdateListener(() -> orderPanel.refreshData());
        }

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();

        // Data Menu (Admin Only)
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JMenu dataMenu = new JMenu("Data");

            JMenuItem backupItem = new JMenuItem("Backup Database");
            backupItem.addActionListener(e -> performBackup());

            JMenuItem restoreItem = new JMenuItem("Restore Database");
            restoreItem.addActionListener(e -> performRestore());

            dataMenu.add(backupItem);
            dataMenu.add(restoreItem);

            menuBar.add(dataMenu);
        }

        // Logout Button in Menu
        JMenu userMenu = new JMenu(currentUser.getUsername());
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        userMenu.add(logoutItem);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        add(tabbedPane);
    }

    private void performBackup() {
        BackupService backupService = new BackupService();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Data Backup (Database + Receipts)");
        fileChooser.setSelectedFile(new File(backupService.generateBackupFileName()));
        fileChooser.setFileFilter(new FileNameExtensionFilter("ZIP Archive", "zip"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            try {
                backupService.backupData(targetFile);
                JOptionPane.showMessageDialog(this, "Backup saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performRestore() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Restoring will OVERWRITE current data (Database & Receipts).\nThis action cannot be undone.\nAre you sure you want to continue?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Backup File");
            fileChooser.setFileFilter(new FileNameExtensionFilter("ZIP Archive", "zip"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sourceFile = fileChooser.getSelectedFile();
                try {
                    new BackupService().restoreData(sourceFile);
                    JOptionPane.showMessageDialog(this,
                            "Data restored successfully!\nPlease restart the application to see changes.", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
