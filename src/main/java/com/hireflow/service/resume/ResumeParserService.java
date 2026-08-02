package com.hireflow.service.resume;

import com.hireflow.domain.Resume;
import com.hireflow.domain.ResumeEducation;
import com.hireflow.domain.ResumeExperience;
import com.hireflow.exception.BusinessRuleException;
import com.hireflow.repository.ResumeEducationRepository;
import com.hireflow.repository.ResumeExperienceRepository;
import com.hireflow.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Resume parsing service utilizing Apache Tika for MIME validation,
 * Apache PDFBox for PDF text extraction, and Apache POI for DOCX text extraction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeParserService {

    private final FileStorageService fileStorageService;
    private final ResumeRepository resumeRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeEducationRepository educationRepository;
    private final Tika tika = new Tika();

    /**
     * Validates that the uploaded file is a valid PDF or DOCX file by inspecting actual magic bytes.
     */
    public String validateAndDetectMimeType(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            String detectedType = tika.detect(is, file.getOriginalFilename());
            log.info("Detected MIME type for file {}: {}", file.getOriginalFilename(), detectedType);

            boolean isPdf = "application/pdf".equals(detectedType);
            boolean isDocx = "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(detectedType)
                    || "application/msword".equals(detectedType);

            if (!isPdf && !isDocx) {
                throw new BusinessRuleException("Only PDF and DOCX resume formats are supported. Detected: " + detectedType);
            }
            return detectedType;
        } catch (Exception e) {
            throw new BusinessRuleException("Failed to validate file type: " + e.getMessage());
        }
    }

    /**
     * Asynchronously parses the uploaded resume and updates the DB entity.
     */
    @Async("taskExecutor")
    @Transactional
    public void parseResumeAsync(Resume resume) {
        log.info("Starting background resume parsing for resumeId: {}", resume.getId());
        resume.setParseStatus("PROCESSING");
        resumeRepository.save(resume);

        try (InputStream is = fileStorageService.getFileAsStream(resume.getS3Key())) {
            String extractedText;
            if ("application/pdf".equalsIgnoreCase(resume.getMimeType())) {
                extractedText = extractTextFromPdf(is);
            } else {
                extractedText = extractTextFromDocx(is);
            }

            resume.setRawText(extractedText);
            resume.setParseStatus("DONE");

            // Extract experience/education section heuristics
            parseAndSaveSections(resume, extractedText);

            resumeRepository.save(resume);
            log.info("Successfully parsed resumeId: {}, length: {} chars", resume.getId(), extractedText.length());
        } catch (Exception e) {
            log.error("Failed to parse resumeId: {}", resume.getId(), e);
            resume.setParseStatus("FAILED");
            resumeRepository.save(resume);
        }
    }

    public String extractTextFromPdf(InputStream inputStream) throws Exception {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public String extractTextFromDocx(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : document.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    private void parseAndSaveSections(Resume resume, String rawText) {
        // Quick regex heuristic section parser for portfolio project
        String lowerText = rawText.toLowerCase();

        if (lowerText.contains("experience") || lowerText.contains("work history")) {
            ResumeExperience experience = ResumeExperience.builder()
                    .resume(resume)
                    .description("Extracted Experience Section from Resume")
                    .build();
            experienceRepository.save(experience);
        }

        if (lowerText.contains("education") || lowerText.contains("university") || lowerText.contains("degree")) {
            ResumeEducation education = ResumeEducation.builder()
                    .resume(resume)
                    .degree("Extracted Degree")
                    .build();
            educationRepository.save(education);
        }
    }
}
