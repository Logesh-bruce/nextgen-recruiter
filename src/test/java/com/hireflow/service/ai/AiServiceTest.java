package com.hireflow.service.ai;

import com.hireflow.dto.request.GenerateQuestionsRequest;
import com.hireflow.dto.response.GeneratedQuestionsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiGateway aiGateway;

    @InjectMocks
    private AiServiceImpl aiService;

    @Test
    @DisplayName("Should generate interview questions via AI gateway")
    void testGenerateQuestions() {
        GenerateQuestionsRequest request = new GenerateQuestionsRequest();
        request.setJobTitle("Backend Engineer");
        request.setSkills(List.of("Java", "Spring Boot"));
        request.setCount(3);

        GeneratedQuestionsResponse expected = new GeneratedQuestionsResponse(List.of(
                new GeneratedQuestionsResponse.QuestionItem("Explain Spring Boot starter auto-configuration.", "technical")
        ));

        when(aiGateway.generateInterviewQuestions("Backend Engineer", List.of("Java", "Spring Boot"), 3)).thenReturn(expected);

        GeneratedQuestionsResponse actual = aiService.generateQuestions(request);

        assertNotNull(actual);
        assertEquals(1, actual.getQuestions().size());
        assertEquals("Explain Spring Boot starter auto-configuration.", actual.getQuestions().get(0).getQuestion());
    }
}
