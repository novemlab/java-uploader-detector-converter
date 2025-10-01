package com.example.uploader.convert;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExcelRewriter {
    public ConversionResult rewriteToXlsx(Path input, Path output, String password, DetectionResult det) {
        try (InputStream in = Files.newInputStream(input)) {
            try (Workbook wbIn = (password == null)
                    ? WorkbookFactory.create(in)
                    : WorkbookFactory.create(in, password);
                 XSSFWorkbook wbOut = new XSSFWorkbook()) {
                copyWorkbook(wbIn, wbOut);
                try (OutputStream out = Files.newOutputStream(output)) {
                    wbOut.write(out);
                }
                return ConversionResult.ok(det, output);
            } catch (EncryptedDocumentException e) {
                return ConversionResult.error(det, ConversionStatus.NEEDS_PASSWORD, "Encrypted Office file");
            }
        } catch (IOException e) {
            return ConversionResult.error(det, ConversionStatus.IO_ERROR, e.getMessage());
        } catch (Exception e) {
            return ConversionResult.error(det, ConversionStatus.PARSE_ERROR, e.getMessage());
        }
    }

    private static void copyWorkbook(Workbook in, XSSFWorkbook out) {
        DataFormatter fmt = new DataFormatter();
        for (int i = 0; i < in.getNumberOfSheets(); i++) {
            Sheet src = in.getSheetAt(i);
            Sheet dst = out.createSheet(safeSheetName(src.getSheetName()));
            int r = 0;
            for (Row row : src) {
                Row newRow = dst.createRow(r++);
                int c = 0;
                for (Cell cell : row) {
                    Cell newCell = newRow.createCell(c++);
                    newCell.setCellValue(fmt.formatCellValue(cell));
                }
            }
        }
    }

    private static String safeSheetName(String s) {
        String n = s.replaceAll("[:\\/?*\[\]]", "_");
        if (n.length() > 31) n = n.substring(0, 31);
        return n;
    }
}

