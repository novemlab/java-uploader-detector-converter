package com.example.uploader.convert;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvToXlsxConverter {
    public Path convert(Path input, Path outputXlsx) throws IOException {
        Charset cs = detectCharset(input);
        CSVFormat fmt = detectFormat(input, cs);

        try (InputStream in = Files.newInputStream(input);
             XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Sheet1");
            try (CSVParser parser = CSVParser.parse(in, cs, fmt)) {
                int r = 0;
                for (CSVRecord rec : parser) {
                    Row row = sh.createRow(r++);
                    for (int c = 0; c < rec.size(); c++) {
                        row.createCell(c).setCellValue(rec.get(c));
                    }
                }
            }
            try (OutputStream out = Files.newOutputStream(outputXlsx)) {
                wb.write(out);
            }
        }
        return outputXlsx;
    }

    private static Charset detectCharset(Path input) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(input))) {
            UniversalDetector detector = new UniversalDetector();
            byte[] buf = in.readNBytes(8192);
            detector.handleData(buf, 0, buf.length);
            detector.dataEnd();
            String cs = detector.getDetectedCharset();
            return cs != null ? Charset.forName(cs) : StandardCharsets.UTF_8;
        }
    }

    private static CSVFormat detectFormat(Path input, Charset cs) throws IOException {
        int commas = 0, tabs = 0, semis = 0;
        try (InputStream in = Files.newInputStream(input)) {
            byte[] buf = in.readNBytes(64 * 1024);
            String s = new String(buf, cs);
            for (String line : s.split("\r?\n")) {
                if (line.isBlank()) continue;
                commas += count(line, ',');
                tabs += count(line, '\t');
                semis += count(line, ';');
            }
        }
        if (tabs >= commas && tabs >= semis) return CSVFormat.TDF;
        if (semis > commas) return CSVFormat.newFormat(';');
        return CSVFormat.DEFAULT;
    }

    private static int count(String s, char ch) {
        int c = 0; for (int i = 0; i < s.length(); i++) if (s.charAt(i)==ch) c++; return c;
    }
}

