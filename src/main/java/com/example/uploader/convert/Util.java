package com.example.uploader.convert;

import java.nio.file.Path;

public class Util {
    public static Path withXlsx(Path out) {
        String s = out.toString();
        if (!s.toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx")) s += ".xlsx";
        return Path.of(s);
    }
}

