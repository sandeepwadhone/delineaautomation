package com.example.secretserver.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelHandler {
    private final String filePath;

    public ExcelHandler(String filePath) {
        this.filePath = filePath;
    }

    public Workbook loadWorkbook() throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return new XSSFWorkbook(fis);
        } catch (IOException e) {
            throw new IOException("Excel file not found or corrupted: " + filePath, e);
        }
    }

    public void saveWorkbook(Workbook workbook, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            workbook.write(fos);
        }
    }

    public String getOutputPath(String suffix) {
        // Logic to append suffix, e.g., secrets.xlsx -> secrets_updated.xlsx
        int dotIndex = filePath.lastIndexOf('.');
        return filePath.substring(0, dotIndex) + suffix + filePath.substring(dotIndex);
    }

    // Helper to get cell value as String (handles null/numeric/etc.)
    public static String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            default: return null;
        }
    }

    // Helper to set cell value (creates cell if null)
    public static void setCellValue(Cell cell, String value) {
        if (cell == null) {
            cell = cell.getRow().createCell(cell.getColumnIndex());
        }
        cell.setCellValue(value);
    }
}