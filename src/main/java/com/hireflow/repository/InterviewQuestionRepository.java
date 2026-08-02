package com.hireflow.repository;

import com.hireflow.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link InterviewQuestion} entity.
 */
@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Integer> {

    List<InterviewQuestion> findByInterviewIdOrderBySortOrderAsc(UUID interviewId);

    void deleteByInterviewId(UUID interviewId);
}
