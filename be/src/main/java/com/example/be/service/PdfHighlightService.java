package com.contractexposed.backend.service;

import com.contractexposed.backend.entity.RiskFinding;
import com.contractexposed.backend.exception.FileProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

/**
 * Annotates a PDF with highlight rectangles over risky clauses.
 * Returns the annotated PDF as a byte array for streaming to the frontend.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PdfHighlightService {

    private final FileStorageService storageService;

    /**
     * Loads the original PDF, adds highlight annotations for each finding,
     * and returns the modified PDF bytes.
     */
    public byte[] generateHighlightedPdf(String storedFileName, List<RiskFinding> findings) {
        Path pdfPath = storageService.resolve(storedFileName);
        try (PDDocument doc = Loader.loadPDF(pdfPath.toFile())) {
            for (RiskFinding finding : findings) {
                if (finding.getOriginalText() == null || finding.getOriginalText().isBlank()) continue;
                addHighlight(doc, finding);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new FileProcessingException("Failed to generate highlighted PDF: " + ex.getMessage(), ex);
        }
    }

    // ─────────────────────────────────────────────────────────

    private void addHighlight(PDDocument doc, RiskFinding finding) throws IOException {
        String searchText = finding.getOriginalText();
        if (searchText.length() > 200) {
            searchText = searchText.substring(0, 200); // search on a snippet
        }

        TextSearchStripper stripper = new TextSearchStripper(searchText);
        stripper.getText(doc); // triggers position collection

        List<TextPosition> positions = stripper.getFoundPositions();
        if (positions.isEmpty()) {
            log.debug("Text not found in PDF for finding: {}", finding.getClauseTitle());
            return;
        }

        int pageIndex = stripper.getFoundPageIndex();
        PDPage page   = doc.getPage(pageIndex);

        float minX = positions.stream().map(TextPosition::getXDirAdj).min(Float::compare).orElse(0f);
        float maxX = positions.stream()
                .map(p -> p.getXDirAdj() + p.getWidthDirAdj()).max(Float::compare).orElse(0f);
        float minY = positions.stream()
                .map(p -> p.getPageHeight() - p.getYDirAdj()).min(Float::compare).orElse(0f);
        float maxY = positions.stream()
                .map(p -> p.getPageHeight() - p.getYDirAdj() + p.getHeightDir()).max(Float::compare).orElse(0f);

        PDAnnotationHighlight highlight = new PDAnnotationHighlight();

        PDRectangle rect = new PDRectangle(minX, minY, maxX - minX, maxY - minY);
        highlight.setRectangle(rect);

        float[] quads = {
            minX, maxY, maxX, maxY,
            minX, minY, maxX, minY
        };
        highlight.setQuadPoints(quads);

        Color awtColor = parseHexColor(finding.getHighlightColor());
        highlight.setColor(new PDColor(
                new float[]{
                    awtColor.getRed()   / 255f,
                    awtColor.getGreen() / 255f,
                    awtColor.getBlue()  / 255f
                },
                PDDeviceRGB.INSTANCE));

        highlight.setContents(finding.getClauseTitle() + ": " + finding.getSeverity());
        highlight.setPrinted(true);
        page.getAnnotations().add(highlight);

        // Update finding page number if not set
        if (finding.getPageNumber() == null) {
            finding.setPageNumber(pageIndex + 1);
        }
    }

    private Color parseHexColor(String hex) {
        try {
            return Color.decode(hex == null ? "#FFFF00" : hex);
        } catch (NumberFormatException e) {
            return Color.YELLOW;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Inner class: PDFTextStripper subclass for text search
    // ─────────────────────────────────────────────────────────

    private static class TextSearchStripper extends PDFTextStripper {

        private final String        searchText;
        private final List<TextPosition> foundPositions = new ArrayList<>();
        private int foundPageIndex = 0;

        public TextSearchStripper(String searchText) throws IOException {
            this.searchText = searchText.toLowerCase().trim();
        }

        @Override
        protected void writeString(String text, List<TextPosition> positions) throws IOException {
            if (!foundPositions.isEmpty()) return; // already found

            String lower = text.toLowerCase();
            int idx = lower.indexOf(searchText.substring(0, Math.min(searchText.length(), 30)));
            if (idx >= 0) {
                int end = Math.min(idx + searchText.length(), positions.size());
                foundPositions.addAll(positions.subList(idx, end));
                foundPageIndex = getCurrentPageNo() - 1;
            }
            super.writeString(text, positions);
        }

        public List<TextPosition> getFoundPositions() { return foundPositions; }
        public int getFoundPageIndex() { return foundPageIndex; }
    }
}
