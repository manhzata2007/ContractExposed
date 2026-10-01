package com.contractexposed.backend.service;

import com.contractexposed.backend.entity.Contract;
import com.contractexposed.backend.exception.FileProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Extracts plain text from PDF, DOCX, DOC, and image files.
 * Falls back to Tess4J OCR when PDF text layer is empty.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TextExtractionService {

    private static final int    MIN_TEXT_LENGTH     = 50;
    private static final float  PDF_RENDER_DPI      = 200f;

    @Value("${app.tesseract.data-path:tessdata}")
    private String tessDataPath;

    @Value("${app.tesseract.language:vie+eng}")
    private String tessLanguage;

    /**
     * Routes extraction based on file type.
     */
    public String extract(Path filePath, Contract.FileType fileType) {
        log.info("Extracting text from {} ({})", filePath.getFileName(), fileType);
        return switch (fileType) {
            case PDF   -> extractPdf(filePath);
            case DOCX  -> extractDocx(filePath);
            case DOC   -> extractDoc(filePath);
            case IMAGE -> extractImage(filePath.toFile());
        };
    }

    // ── PDF ───────────────────────────────────────────────────

    private String extractPdf(Path path) {
        try (PDDocument doc = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc).trim();

            if (text.length() < MIN_TEXT_LENGTH) {
                log.info("PDF text layer too short ({}c) — switching to OCR", text.length());
                text = ocrPdf(doc);
            }
            return text;
        } catch (IOException ex) {
            throw new FileProcessingException("Failed to read PDF: " + ex.getMessage(), ex);
        }
    }

    private String ocrPdf(PDDocument doc) throws IOException {
        PDFRenderer renderer = new PDFRenderer(doc);
        StringBuilder sb = new StringBuilder();
        for (int page = 0; page < doc.getNumberOfPages(); page++) {
            BufferedImage image = renderer.renderImageWithDPI(page, PDF_RENDER_DPI, ImageType.RGB);
            sb.append(runOcr(image));
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    // ── DOCX ──────────────────────────────────────────────────

    private String extractDocx(Path path) {
        try (XWPFDocument doc = new XWPFDocument(java.nio.file.Files.newInputStream(path));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText().trim();
        } catch (IOException ex) {
            throw new FileProcessingException("Failed to read DOCX: " + ex.getMessage(), ex);
        }
    }

    // ── DOC (legacy) ──────────────────────────────────────────

    private String extractDoc(Path path) {
        try (HWPFDocument doc = new HWPFDocument(java.nio.file.Files.newInputStream(path));
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText().trim();
        } catch (IOException ex) {
            throw new FileProcessingException("Failed to read DOC: " + ex.getMessage(), ex);
        }
    }

    // ── Image ─────────────────────────────────────────────────

    private String extractImage(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new FileProcessingException("Cannot read image file: " + file.getName());
            }
            return runOcr(image);
        } catch (IOException ex) {
            throw new FileProcessingException("Failed to read image: " + ex.getMessage(), ex);
        }
    }

    // ── OCR engine ────────────────────────────────────────────

    private String runOcr(BufferedImage image) {
        Tesseract tess = new Tesseract();
        tess.setDatapath(tessDataPath);
        tess.setLanguage(tessLanguage);
        tess.setPageSegMode(1);   // Auto page segmentation with OSD
        tess.setOcrEngineMode(1); // LSTM engine
        try {
            return tess.doOCR(image).trim();
        } catch (TesseractException ex) {
            log.warn("OCR failed: {}", ex.getMessage());
            return "";
        }
    }
}
