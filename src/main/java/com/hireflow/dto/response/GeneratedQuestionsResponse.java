package com.hireflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuestionsResponse {

    private List<QuestionItem> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionItem {
        private String question;
        private String category;
    }
}
