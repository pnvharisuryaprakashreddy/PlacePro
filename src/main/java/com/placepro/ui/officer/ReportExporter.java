package com.placepro.ui.officer;

import com.placepro.service.report.ReportTable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exports a ReportTable to CSV or a simple paginated PDF (Apache PDFBox).
 */
final class ReportExporter {

    private static final float MARGIN = 36f;
    private static final float ROW_HEIGHT = 16f;
    private static final float HEADER_FONT_SIZE = 9f;
    private static final float BODY_FONT_SIZE = 8f;
    private static final float TITLE_FONT_SIZE = 14f;

    private ReportExporter() {
    }

    static void toCsv(File file, ReportTable report) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8.name())) {
            writer.println(csvLine(report.getColumns().toArray()));
            for (List<Object> row : report.getRows()) {
                writer.println(csvLine(row.toArray()));
            }
        }
    }

    private static String csvLine(Object[] values) {
        StringBuilder line = new StringBuilder();
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                line.append(',');
            }
            String text = values[index] == null ? "" : values[index].toString();
            if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
                text = "\"" + text.replace("\"", "\"\"") + "\"";
            }
            line.append(text);
        }
        return line.toString();
    }

    static void toPdf(File file, ReportTable report) throws IOException {
        PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        float usableWidth = landscape.getWidth() - 2 * MARGIN;
        int columnCount = report.getColumns().size();
        float columnWidth = usableWidth / columnCount;
        // Rough character budget per cell for the monospaced-ish truncation below.
        int charsPerCell = Math.max(4, (int) (columnWidth / (BODY_FONT_SIZE * 0.55f)));

        try (PDDocument document = new PDDocument()) {
            int rowIndex = 0;
            List<List<Object>> rows = report.getRows();
            boolean firstPage = true;

            while (rowIndex < rows.size() || firstPage) {
                PDPage page = new PDPage(landscape);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    float y = landscape.getHeight() - MARGIN;

                    if (firstPage) {
                        content.beginText();
                        content.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
                        content.newLineAtOffset(MARGIN, y);
                        content.showText(report.getTitle());
                        content.endText();
                        y -= TITLE_FONT_SIZE + 10;
                    }

                    y = writeRow(content, report.getColumns().toArray(), y, columnWidth,
                            charsPerCell, PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);

                    while (rowIndex < rows.size() && y > MARGIN + ROW_HEIGHT) {
                        y = writeRow(content, rows.get(rowIndex).toArray(), y, columnWidth,
                                charsPerCell, PDType1Font.HELVETICA, BODY_FONT_SIZE);
                        rowIndex++;
                    }
                }
                firstPage = false;
            }
            document.save(file);
        }
    }

    private static float writeRow(PDPageContentStream content,
                                  Object[] values,
                                  float y,
                                  float columnWidth,
                                  int charsPerCell,
                                  org.apache.pdfbox.pdmodel.font.PDFont font,
                                  float fontSize) throws IOException {
        float x = MARGIN;
        for (Object value : values) {
            String text = value == null ? "" : value.toString();
            if (text.length() > charsPerCell) {
                text = text.substring(0, Math.max(1, charsPerCell - 3)) + "...";
            }
            content.beginText();
            content.setFont(font, fontSize);
            content.newLineAtOffset(x, y);
            content.showText(sanitize(text));
            content.endText();
            x += columnWidth;
        }
        return y - ROW_HEIGHT;
    }

    /** PDFBox standard fonts cannot encode all characters; replace what they can't. */
    private static String sanitize(String text) {
        StringBuilder safe = new StringBuilder(text.length());
        for (char character : text.toCharArray()) {
            safe.append(character < 32 || character > 255 ? '?' : character);
        }
        return safe.toString();
    }
}
