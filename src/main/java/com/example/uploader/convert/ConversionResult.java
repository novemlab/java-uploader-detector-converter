package com.example.uploader.convert;

import java.nio.file.Path;

public record ConversionResult(
        DetectionResult detection,
        ConversionStatus status,
        Path output,
        String message
) {
    public static ConversionResult ok(DetectionResult d, Path output) {
        return new ConversionResult(d, ConversionStatus.OK, output, "");
    }
    public static ConversionResult error(DetectionResult d, ConversionStatus s, String msg) {
        return new ConversionResult(d, s, null, msg);
    }
}

