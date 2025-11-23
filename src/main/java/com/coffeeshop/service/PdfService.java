package com.coffeeshop.service;

import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfService {

    public void generateReceipt(Order order) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String directoryPath = "receipts";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = directoryPath + File.separator + "Receipt_" + order.getId() + "_" + timestamp + ".pdf";
            File file = new File(fileName);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.DARK_GRAY);
            Paragraph title = new Paragraph("Coffee Shop Receipt", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            // Order Details
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

            document.add(new Paragraph("Order ID: " + order.getId(), boldFont));
            document.add(new Paragraph("Customer: " + order.getCustomerName(), normalFont));

            SimpleDateFormat prettyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            document.add(new Paragraph("Date: " + prettyDate.format(new Date()), normalFont));
            document.add(new Paragraph("\n"));

            // Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Table Headers
            String[] headers = { "Product", "Qty", "Price", "Subtotal" };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Table Data
            for (OrderItem item : order.getItems()) {
                table.addCell(new Phrase(item.getProductName()));
                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity())));
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(qtyCell);

                PdfPCell priceCell = new PdfPCell(new Phrase(String.format("$%.2f", item.getPriceAtPurchase())));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(priceCell);

                PdfPCell subtotalCell = new PdfPCell(new Phrase(String.format("$%.2f", item.getSubtotal())));
                subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(subtotalCell);
            }

            document.add(table);

            // Total
            Paragraph total = new Paragraph("Total Amount: $" + String.format("%.2f", order.getTotalAmount()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // Footer
            Paragraph footer = new Paragraph("\nThank you for visiting!",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            System.out.println("Receipt generated at: " + file.getAbsolutePath());

            // Automatically open the file
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(null, "Receipt saved to: " + file.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating receipt: " + e.getMessage());
        }
    }
}
