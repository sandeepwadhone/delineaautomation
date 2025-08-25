package com.example.secretserver;

import com.example.secretserver.api.SecretServerClient;
import com.example.secretserver.config.AppConfig;
import com.example.secretserver.excel.ExcelHandler;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String EXCEL_FILE_PATH = "C:\\Users\\ACER\\Documents\\sandeep\\secrets.xlsx";

    public static void main(String[] args) {
        try {
            // Load configuration for API settings
            Properties config = AppConfig.loadProperties();
            logger.info("Using hardcoded Excel file path: {}", EXCEL_FILE_PATH);

            // Initialize Excel handler and load workbook
            ExcelHandler excelHandler = new ExcelHandler(EXCEL_FILE_PATH);
            Workbook workbook = excelHandler.loadWorkbook();
            Sheet sheet = workbook.getSheetAt(0);  // Assume first sheet

            // Initialize API client
            SecretServerClient apiClient = new SecretServerClient(
                    config.getProperty("secret.server.base.url"),
                    config.getProperty("auth.token")
            );

            // Process each row starting from row 2
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // 0-based, skip header (row 0)
                Row row = sheet.getRow(i);
                if (row == null) continue;

                processRow(row, apiClient);
            }

            // Save updated Excel
            String outputPath = excelHandler.getOutputPath(config.getProperty("output.file.suffix"));
            excelHandler.saveWorkbook(workbook, outputPath);
            logger.info("Processing complete. Updated file saved at: {}", outputPath);

        } catch (IOException e) {
            logger.error("Excel file issue: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
    }

    private static void processRow(Row row, SecretServerClient apiClient) {
        try {
            String secretId = ExcelHandler.getCellValue(row.getCell(0));
            String secretName = ExcelHandler.getCellValue(row.getCell(1));

            if (secretId == null || secretName == null) {
                logger.warn("Skipping row {}: secretid or secretname is null", row.getRowNum() + 1);
                return;
            }

            SecretServerClient.SecretDetails details = apiClient.getSecretDetails(secretId, 3);
            if (details != null) {
                ExcelHandler.setCellValue(row.getCell(2), details.name);
                ExcelHandler.setCellValue(row.getCell(3), details.status);
            } else {
                ExcelHandler.setCellValue(row.getCell(3), "API Call Failed");
            }

            boolean disableSuccess = apiClient.disableSecret(secretId, 3);
            if (disableSuccess) {
                ExcelHandler.setCellValue(row.getCell(3), "Disabled");
                ExcelHandler.setCellValue(row.getCell(4), "Success");
            } else {
                ExcelHandler.setCellValue(row.getCell(4), "Fail");
            }
        } catch (Exception e) {
            logger.error("Error processing row {}: {}", row.getRowNum() + 1, e.getMessage(), e);
        }
    }
}