# Java Uploader Detector & Converter

A small library for Spring Boot 3 services to **detect** uploaded spreadsheet-like files and **convert** them to clean `.xlsx`.

Supported inputs:
- Real Excel: `.xlsx` (ZIP/OOXML), `.xls` (OLE2/BIFF8)
- **HTML masquerading as `.xls`** (tables in HTML)
- CSV/TSV (any extension)

Outputs:
- Valid `.xlsx` (multi-sheet when relevant)

## Quick start
```java
var svc = new UploaderDetectorConverter();
var res = svc.detectAndConvert(inputPath, outputXlsxPath, null /*password if needed*/);
if (res.status() == ConversionStatus.OK) {
    System.out.println("Wrote: " + res.output());
} else {
    System.out.println(res.status() + ": " + res.message());
}
```
