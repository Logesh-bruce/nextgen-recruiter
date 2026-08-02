package com.hireflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeEducationResponse {

    private String degree;
    private String fieldOfStudy;
    private String institution;
    private Short graduationYear;
    private BigDecimal gpa;
}
