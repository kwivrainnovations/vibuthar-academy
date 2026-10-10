package com.academy.project.util;

import com.academy.project.dto.user.BulkCreateUserItem;
import com.academy.project.dto.user.BulkUserParseResult;
import com.academy.project.dto.user.BulkUserRowError;
import com.academy.project.entity.user.UserRole;
import com.academy.project.exception.ApiException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Sample Excel template + parse for bulk user import.
 *
 * Columns: Full name | Mobile number | Password | Role
 * Role must be STUDENT, TRAINER, or ADMIN (defaults to STUDENT when blank).
 */
public final class UserExcelHelper {

    public static final String SAMPLE_FILENAME = "users-sample.xlsx";
    public static final String ERROR_REPORT_FILENAME = "users-errors.xlsx";

    private static final String[] HEADERS = {
            "Full name", "Mobile number", "Password", "Role"
    };

    private static final String[] ERROR_HEADERS = {
            "Full name", "Mobile number", "Password", "Role", "Error"
    };

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "application/octet-stream"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");

    private UserExcelHelper() {
    }

    public static byte[] buildSampleWorkbook() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet users = workbook.createSheet("Users");
            Sheet instructions = workbook.createSheet("Instructions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = users.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Example rows — replace or delete before upload
            writeRow(users, 1, "Anita Sharma", "9876543210", "Password@123", "STUDENT");
            writeRow(users, 2, "Ravi Kumar", "9123456780", "Password@123", "STUDENT");

            for (int i = 0; i < HEADERS.length; i++) {
                users.autoSizeColumn(i);
            }

            String[] lines = {
                    "How to use this template",
                    "1. Fill each row with one user on the Users sheet.",
                    "2. Full name, Mobile number, and Password are required.",
                    "3. Password must be at least 8 characters.",
                    "4. Mobile number must be 7–15 digits (optional leading +).",
                    "5. Role is optional. Must be STUDENT, TRAINER, or ADMIN. Blank defaults to STUDENT.",
                    "6. Invalid rows are skipped; valid rows are still created. Check the response errors list.",
                    "7. Download failed rows via POST /api/admin/users/excel/error-report (send the errors from the upload response).",
                    "8. Delete the sample example rows before uploading (or keep them if you want those users).",
                    "9. Save as .xlsx and upload via POST /api/admin/users/excel"
            };
            for (int i = 0; i < lines.length; i++) {
                Row row = instructions.createRow(i);
                row.createCell(0).setCellValue(lines[i]);
            }
            instructions.autoSizeColumn(0);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new ApiException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate sample Excel: " + ex.getMessage()
            );
        }
    }

    /**
     * Parses the workbook. File/header problems fail the whole request.
     * Per-row validation problems are collected so valid rows can still be created.
     */
    public static BulkUserParseResult parse(MultipartFile file) {
        validateFile(file);

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheet("Users");
            if (sheet == null) {
                sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            }
            if (sheet == null) {
                throw ApiException.badRequest("Excel file has no sheets");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw ApiException.badRequest("Excel file is missing a header row");
            }
            validateHeaders(headerRow);

            List<BulkCreateUserItem> validItems = new ArrayList<>();
            List<BulkUserRowError> errors = new ArrayList<>();
            Set<String> phonesInFile = new HashSet<>();
            int totalRows = 0;
            int lastRow = sheet.getLastRowNum();

            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isBlankRow(row)) {
                    continue;
                }

                totalRows++;
                int excelRow = r + 1; // 1-based for error messages
                String name = cellString(row.getCell(0));
                String phone = cellString(row.getCell(1));
                String password = cellString(row.getCell(2));
                String roleRaw = cellString(row.getCell(3));

                String rowError = validateRow(name, phone, password, roleRaw);
                if (rowError != null) {
                    errors.add(rowError(excelRow, rowError, name, phone, password, roleRaw));
                    continue;
                }

                String phoneKey = PhoneUtils.normalize(phone);
                if (!phonesInFile.add(phoneKey)) {
                    errors.add(rowError(
                            excelRow,
                            "Duplicate mobile number in the Excel file",
                            name, phone, password, roleRaw
                    ));
                    continue;
                }

                BulkCreateUserItem item = new BulkCreateUserItem();
                item.setSourceRow(excelRow);
                item.setName(name);
                item.setPhone(phone);
                item.setPassword(password);
                item.setRole(parseRole(roleRaw));
                validItems.add(item);
            }

            if (totalRows == 0) {
                throw ApiException.badRequest("Excel has no user rows. Add at least one user below the header.");
            }

            return BulkUserParseResult.builder()
                    .totalRows(totalRows)
                    .validItems(validItems)
                    .errors(errors)
                    .build();
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.badRequest("Failed to read Excel file: " + ex.getMessage());
        }
    }

    /** Builds an Excel containing only failed rows + an Error column for fix-and-retry. */
    public static byte[] buildErrorReportWorkbook(List<BulkUserRowError> errors) {
        if (errors == null || errors.isEmpty()) {
            throw ApiException.badRequest("No errored rows to download");
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Errors");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < ERROR_HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(ERROR_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (BulkUserRowError error : errors) {
                writeRow(sheet, rowIndex++,
                        nullToEmpty(error.getFullName()),
                        nullToEmpty(error.getMobileNumber()),
                        nullToEmpty(error.getPassword()),
                        nullToEmpty(error.getRole()),
                        nullToEmpty(error.getMessage()));
            }

            for (int i = 0; i < ERROR_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            Sheet instructions = workbook.createSheet("Instructions");
            String[] lines = {
                    "Error report for bulk user upload",
                    "1. Each row below failed validation or could not be created.",
                    "2. Fix the values using the Error column as a guide.",
                    "3. You can re-upload this file as-is (the Error column is ignored on import).",
                    "4. Re-upload via POST /api/admin/users/excel"
            };
            for (int i = 0; i < lines.length; i++) {
                Row row = instructions.createRow(i);
                row.createCell(0).setCellValue(lines[i]);
            }
            instructions.autoSizeColumn(0);

            workbook.write(out);
            return out.toByteArray();
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate error Excel: " + ex.getMessage()
            );
        }
    }

    public static BulkUserRowError rowError(
            int row,
            String message,
            String fullName,
            String mobileNumber,
            String password,
            String role) {
        return BulkUserRowError.builder()
                .row(row)
                .message(message)
                .fullName(fullName)
                .mobileNumber(mobileNumber)
                .password(password)
                .role(role)
                .build();
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private static String validateRow(String name, String phone, String password, String roleRaw) {
        if (name == null) {
            return "Full name is required";
        }
        if (name.length() > 150) {
            return "Full name must be at most 150 characters";
        }
        if (phone == null) {
            return "Mobile number is required";
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "Mobile number must be a valid phone number";
        }
        if (password == null) {
            return "Password is required";
        }
        if (password.length() < 8 || password.length() > 100) {
            return "Password must be 8–100 characters";
        }
        if (roleRaw != null && !roleRaw.isBlank()) {
            try {
                UserRole.valueOf(roleRaw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return "Role must be STUDENT, TRAINER, or ADMIN (got '" + roleRaw + "')";
            }
        }
        return null;
    }

    private static UserRole parseRole(String raw) {
        if (raw == null || raw.isBlank()) {
            return UserRole.STUDENT;
        }
        return UserRole.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Excel file is required");
        }
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            throw ApiException.badRequest("Only Excel files (.xlsx) are allowed");
        }
        String contentType = file.getContentType();
        if (contentType != null
                && !contentType.isBlank()
                && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            if (!contentType.contains("sheet") && !contentType.contains("excel") && !contentType.contains("octet")) {
                throw ApiException.badRequest("Only Excel files (.xlsx) are allowed");
            }
        }
    }

    private static void validateHeaders(Row headerRow) {
        for (int i = 0; i < HEADERS.length; i++) {
            String actual = cellString(headerRow.getCell(i));
            if (actual == null || !HEADERS[i].equalsIgnoreCase(actual.trim())) {
                throw ApiException.badRequest(
                        "Invalid header in column " + (i + 1) + ". Expected '" + HEADERS[i]
                                + "'. Download the sample Excel and keep the header row."
                );
            }
        }
    }

    private static void writeRow(Sheet sheet, int rowIndex, String... values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i] != null ? values[i] : "");
        }
    }

    private static boolean isBlankRow(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            if (cellString(row.getCell(i)) != null) {
                return false;
            }
        }
        return true;
    }

    private static String cellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String value = switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    yield String.valueOf((long) num);
                }
                yield String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception ex) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BLANK -> null;
            default -> null;
        };
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }
}
