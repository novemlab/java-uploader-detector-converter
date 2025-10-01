package com.example.uploader.convert;

import java.nio.file.Path;

public record DetectionResult(
        Path input,
        DetectedKind kind,
        String details
) {}

