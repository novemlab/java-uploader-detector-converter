package com.example.uploader.convert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploaderDetectorConverter {
    private final FileSniffer sniffer = new FileSniffer();
    private final HtmlTableToXlsxConverter htmlConv = new HtmlTableToXlsxConverter();
    private final CsvToXlsxConverter csvConv = new CsvToXlsxConverter();
    private final ExcelRewriter excelRewriter = new ExcelRewriter();

    public ConversionResult detectAndConvert(Path input, Path outputXlsx, String password) {
        try {
            DetectionResult det = sniffer.detect(input);
            Path out = Util.withXlsx(outputXlsx);
            return switch (det.kind()) {
                case XLSX_ZIP, XLS_OLE2 -> excelRewriter.rewriteToXlsx(input, out, password, det);
                case HTML_TABLES -> {
                    try {
                        Path p = htmlConv.convert(input, out);
                        yield ConversionResult.ok(det, p);
                    } catch (IOException e) {
                        yield ConversionResult.error(det, ConversionStatus.PARSE_ERROR, e.getMessage());
                    }
                }
                case TEXT_CSV_TSV -> {
                    try {
                        Path p = csvConv.convert(input, out);
                        yield ConversionResult.ok(det, p);
                    } catch (IOException e) {
                        yield ConversionResult.error(det, ConversionStatus.PARSE_ERROR, e.getMessage());
                    }
                }
                case UNKNOWN -> ConversionResult.error(det, ConversionStatus.UNSUPPORTED, "Unsupported or corrupt file");
            };
        } catch (IOException e) {
            return ConversionResult.error(new DetectionResult(input, DetectedKind.UNKNOWN, ""), ConversionStatus.IO_ERROR, e.getMessage());
        }
    }

    public ConversionResult detectConvertEnsuringDir(Path input, Path outputXlsx, String password) {
        try { Files.createDirectories(outputXlsx.toAbsolutePath().getParent()); } catch (Exception ignored) {}
        return detectAndConvert(input, outputXlsx, password);
    }
}

