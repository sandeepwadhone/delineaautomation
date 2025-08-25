// changes 3 00:18
package com.example.secretserver.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExcelHandler.class);
    private final String filePath;

    public ExcelHandler(String filePath) {
        this.filePath = filePath;
    }

    public Workbook loadWorkbook() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("Excel file does not exist at path: {}", filePath);
            throw new IOException("Excel file does not exist at path: " + filePath);
        }
        if (!file.canRead()) {
            logger.error("Cannot read Excel file due to permissions: {}", filePath);
            throw new IOException("Cannot read Excel file due to permissions: " + filePath);
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return new XSSFWorkbook(fis);
        } catch (IOException e) {
            logger.error("Excel file is corrupted or invalid: {}", filePath, e);
            throw new IOException("Excel file is corrupted or invalid: " + filePath, e);
        }
    }

    public void saveWorkbook(Workbook workbook, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            workbook.write(fos);
        } catch (IOException e) {
            logger.error("Failed to save Excel file at: {}", outputPath, e);
            throw e;
        }
    }

    public String getOutputPath(String suffix) {
        int dotIndex = filePath.lastIndexOf('.');
        return filePath.substring(0, dotIndex) + suffix + filePath.substring(dotIndex);
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                // Format as integer if no decimal part
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            default:
                return null;
        }
    }

    public static void setCellValue(Cell cell, String value) {
        if (cell == null) {
            logger.error("Cannot set value: Cell is null");
            throw new IllegalArgumentException("Cell is null");
        }
        cell.setCellValue(value);
    }
}
