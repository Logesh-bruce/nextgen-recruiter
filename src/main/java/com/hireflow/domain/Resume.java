package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code resumes} table.
 *
 * <p>Tracks uploaded resume files and their parsing lifecycle.
 * {@code parseStatus} values: PENDING → PROCESSING → DONE | FAILED
 * The raw extracted text is stored in {@code rawText} for AI scoring.
 */
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** S3 object key (or local path for dev). */
    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Column(name = "file_size_bytes", nullable = false)
    private Integer fileSizeBytes;

    /** MIME type — validated by Apache Tika, not file extension. */
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    /** Full plain-text extracted from the resume for AI processing. */
    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    /** PENDING | PROCESSING | DONE | FAILED */
    @Column(name = "parse_status", nullable = false, length = 20)
    @Builder.Default
    private String parseStatus = "PENDING";

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean isPrimary = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
