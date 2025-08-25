// changes 4 00:50

package com.example.secretserver;

import com.example.secretserver.api.SecretServerClient;
import com.example.secretserver.config.AppConfig;
import com.example.secretserver.excel.ExcelHandler;
import org.apache.poi.ss.usermodel.Cell;
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
            logger.info("Starting to open Excel file: {}", EXCEL_FILE_PATH);
            Workbook workbook = excelHandler.loadWorkbook();
            logger.info("Excel file opened successfully");
            Sheet sheet = workbook.getSheetAt(0);  // Assume first sheet
            logger.info("Loaded sheet: {}, Total rows: {}", sheet.getSheetName(), sheet.getLastRowNum() + 1);

            // Initialize API client
            logger.info("Initializing SecretServerClient with base URL: {}", config.getProperty("secret.server.base.url"));
            SecretServerClient apiClient = new SecretServerClient(
                    config.getProperty("secret.server.base.url"),
                    config.getProperty("auth.token")
            );
            logger.info("SecretServerClient initialized successfully");

            // Process each row starting from row 2
            logger.info("Starting to process rows");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // 0-based, skip header (row 0)
                Row row = sheet.getRow(i);
                if (row == null) {
                    logger.warn("Skipping row {}: Row is null", i + 1);
                    continue;
                }
                logger.info("Starting to process row {} (Excel row {})", i + 1, row.getRowNum() + 1);
                processRow(row, apiClient);
                logger.info("Finished processing row {} (Excel row {})", i + 1, row.getRowNum() + 1);
            }
            logger.info("Finished processing all rows");

            // Save updated Excel
            String outputPath = excelHandler.getOutputPath(config.getProperty("output.file.suffix"));
            logger.info("Starting to save updated Excel file to: {}", outputPath);
            excelHandler.saveWorkbook(workbook, outputPath);
            logger.info("Updated Excel file saved successfully at: {}", outputPath);

        } catch (IOException e) {
            logger.error("Excel file issue: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
    }

    private static void processRow(Row row, SecretServerClient apiClient) {
        try {
            Cell secretIdCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell secretNameCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String secretId = ExcelHandler.getCellValue(secretIdCell);
            String secretName = ExcelHandler.getCellValue(secretNameCell);

            if (secretId == null || secretId.trim().isEmpty() || secretName == null || secretName.trim().isEmpty()) {
                logger.warn("Skipping row {}: secretid or secretname is null or empty", row.getRowNum() + 1);
                return;
            }
            logger.debug("Read secretId: {}, secretName: {}", secretId, secretName);

            // Log payload for getSecretDetails API call
            String getUrl = apiClient.getBaseUrl() + "/secrets/" + secretId;
            logger.info("getUrl: {}", getUrl);
            logger.info("API payload for getSecretDetails: URL={}, Headers=[Authorization: Bearer <redacted>, Accept: application/json, User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36]");
            logger.info("Starting API call to get details for secretId {}", secretId);
            SecretServerClient.SecretDetails details = apiClient.getSecretDetails(secretId, 3);
            logger.info("API call to get details for secretId {} completed", secretId);
            if (details != null) {
                // Ensure cells exist before setting values
                Cell secretNameFromSSCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell statusCell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                ExcelHandler.setCellValue(secretNameFromSSCell, details.name);
                ExcelHandler.setCellValue(statusCell, details.status);
            } else {
                // Ensure status cell exists
                Cell statusCell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                ExcelHandler.setCellValue(statusCell, "API Call Failed");
            }

            // Log payload for disableSecret API call
            String disableUrl = apiClient.getBaseUrl() + "/secrets/" + secretId + "/disable";
            logger.info("disableUrl: {}", disableUrl);
            logger.info("API payload for disableSecret: URL={}, Headers=[Authorization: Bearer <redacted>, Accept: application/json, User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36]");
            logger.info("Starting API call to disable secretId {}", secretId);
            boolean disableSuccess = apiClient.disableSecret(secretId, 3);
            logger.info("API call to disable secretId {} completed", secretId);
            // Ensure cells exist
            Cell statusCell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell actionCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (disableSuccess) {
                ExcelHandler.setCellValue(statusCell, "Disabled");
                ExcelHandler.setCellValue(actionCell, "Success");
            } else {
                ExcelHandler.setCellValue(actionCell, "Fail");
            }
        } catch (Exception e) {
            logger.error("Error processing row {}: {}", row.getRowNum() + 1, e.getMessage(), e);
        }
    }
}
