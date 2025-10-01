package com.example.uploader.convert;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class HtmlTableToXlsxConverter {
    public Path convert(Path input, Path outputXlsx) throws IOException {
        try (InputStream in = Files.newInputStream(input);
             XSSFWorkbook wb = new XSSFWorkbook()) {

            Document doc = Jsoup.parse(in, null, "");
            Elements tables = doc.select("table");
            if (tables.isEmpty()) {
                throw new IOException("No <table> elements found in HTML");
            }
            int idx = 1;
            for (Element table : tables) {
                String name = safeSheetName("Table_" + idx++);
                Sheet sh = wb.createSheet(name);
                Elements rows = table.select("tr");
                int r = 0;
                for (Element tr : rows) {
                    Row row = sh.createRow(r++);
                    Elements cells = tr.select("th, td");
                    int c = 0;
                    for (Element td : cells) {
                        Cell cell = row.createCell(c++);
                        cell.setCellValue(td.text());
                    }
                }
            }
            try (OutputStream out = Files.newOutputStream(outputXlsx)) {
                wb.write(out);
            }
        }
        return outputXlsx;
    }

    private static String safeSheetName(String s) {
        if (s == null || s.isEmpty()) {
            return "Sheet";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length() && out.length() < 31; i++) {
            char ch = s.charAt(i);
            boolean invalid = (ch == ':' || ch == '\\' || ch == '/' || ch == '?' || ch == '*' || ch == '[' || ch == ']');
            out.append(invalid ? '_' : ch);
        }
        if (out.length() == 0) {
            return "Sheet";
        }
        return out.toString();
    }
}
