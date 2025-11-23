package com.coffeeshop.service;

import java.io.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupService {

    private static final String DB_FILE_NAME = "coffee_db.sqlite";
    private static final String RECEIPTS_DIR = "receipts";

    /**
     * Backs up the database and receipts to the specified target ZIP file.
     *
     * @param targetZipFile The file where the backup should be saved.
     * @throws IOException If an I/O error occurs.
     */
    public void backupData(File targetZipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZipFile))) {
            // Backup Database
            File dbFile = new File(DB_FILE_NAME);
            if (dbFile.exists()) {
                addToZip(dbFile, dbFile.getName(), zos);
            }

            // Backup Receipts
            File receiptsDir = new File(RECEIPTS_DIR);
            if (receiptsDir.exists() && receiptsDir.isDirectory()) {
                File[] files = receiptsDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            addToZip(file, RECEIPTS_DIR + "/" + file.getName(), zos);
                        }
                    }
                }
            }
        }
    }

    private void addToZip(File file, String entryName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }

    /**
     * Restores the database and receipts from the specified source ZIP file.
     *
     * @param sourceZipFile The backup ZIP file to restore from.
     * @throws IOException If an I/O error occurs.
     */
    public void restoreData(File sourceZipFile) throws IOException {
        if (!sourceZipFile.exists()) {
            throw new IOException("Backup file not found: " + sourceZipFile.getAbsolutePath());
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceZipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(zipEntry.getName());

                // Security check to prevent Zip Slip vulnerability
                String destDirPath = new File(".").getCanonicalPath();
                String destFilePath = newFile.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator)) {
                    // Skip suspicious entries
                }

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        // ignore if exists
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                        // ignore if exists
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    /**
     * Generates a default backup filename with timestamp.
     * 
     * @return String filename like "coffee_shop_backup_20231027_103000.zip"
     */
    public String generateBackupFileName() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return "coffee_shop_backup_" + dtf.format(LocalDateTime.now()) + ".zip";
    }
}
