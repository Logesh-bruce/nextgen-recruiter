package com.hireflow.mapper;

import com.hireflow.domain.Interview;
import com.hireflow.domain.InterviewQuestion;
import com.hireflow.dto.response.InterviewQuestionResponse;
import com.hireflow.dto.response.InterviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Interview and InterviewQuestion entity conversions.
 */
@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "applicationId", source = "application.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "candidateName", expression = "java(interview.getApplication().getCandidate().getUser().getFirstName() + ' ' + interview.getApplication().getCandidate().getUser().getLastName())")
    InterviewResponse toInterviewResponse(Interview interview);

    InterviewQuestionResponse toQuestionResponse(InterviewQuestion question);
}
