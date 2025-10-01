package com.example.uploader.convert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class FileSniffer {
    public DetectionResult detect(Path input) throws IOException {
        byte[] head;
        try (InputStream in = Files.newInputStream(input)) {
            head = in.readNBytes(2048);
        }
        if (head.length == 0) {
            return new DetectionResult(input, DetectedKind.UNKNOWN, "empty file");
        }
        if (startsWith(head, new byte[]{(byte)0xD0,(byte)0xCF,0x11,(byte)0xE0,(byte)0xA1,(byte)0xB1,0x1A,(byte)0xE1})) {
            return new DetectionResult(input, DetectedKind.XLS_OLE2, "OLE2/CFB container");
        }
        if (startsWith(head, new byte[]{0x50,0x4B,0x03,0x04})) {
            return new DetectionResult(input, DetectedKind.XLSX_ZIP, "ZIP/OOXML container");
        }
        var s = new String(head, java.nio.charset.StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        if (s.contains("<html") || s.contains("<!doctype html") || s.contains("<table")) {
            return new DetectionResult(input, DetectedKind.HTML_TABLES, "HTML with tables");
        }
        if (s.indexOf(',') >= 0 || s.indexOf('\t') >= 0 || s.indexOf(';') >= 0) {
            return new DetectionResult(input, DetectedKind.TEXT_CSV_TSV, "text with delimiters");
        }
        return new DetectionResult(input, DetectedKind.UNKNOWN, "unrecognized header");
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if (data[i] != prefix[i]) return false;
        return true;
    }
}

