package com.hireflow.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireflow.domain.Application;
import com.hireflow.domain.Candidate;
import com.hireflow.domain.MatchScore;
import com.hireflow.dto.response.ApplicationResponse;
import com.hireflow.dto.response.ApplicationSummaryResponse;
import com.hireflow.dto.response.CandidateSummaryResponse;
import com.hireflow.dto.response.MatchScoreResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for Application and MatchScore entity conversions.
 */
@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "candidate", source = "candidate", qualifiedByName = "mapCandidateSummary")
    @Mapping(target = "resumeId", source = "resume.id")
    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "matchScore", ignore = true) // Handled in service layer via MatchScore object mapping
    ApplicationResponse toApplicationResponse(Application application);

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyName", source = "job.recruiter.companyName")
    @Mapping(target = "candidate", source = "candidate", qualifiedByName = "mapCandidateSummary")
    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "matchScore", ignore = true)
    ApplicationSummaryResponse toApplicationSummaryResponse(Application application);

    @Named("mapCandidateSummary")
    default CandidateSummaryResponse mapCandidateSummary(Candidate candidate) {
        if (candidate == null) return null;
        return CandidateSummaryResponse.builder()
                .id(candidate.getId())
                .firstName(candidate.getUser().getFirstName())
                .lastName(candidate.getUser().getLastName())
                .headline(candidate.getHeadline())
                .location(candidate.getLocation())
                .build();
    }

    default MatchScoreResponse toMatchScoreResponse(MatchScore matchScore) {
        if (matchScore == null) return null;
        return MatchScoreResponse.builder()
                .score(matchScore.getScore())
                .matchedSkills(parseJsonList(matchScore.getMatchedSkills()))
                .missingSkills(parseJsonList(matchScore.getMissingSkills()))
                .experienceGap(matchScore.getExperienceGap())
                .aiSummary(matchScore.getAiSummary())
                .build();
    }

    private static List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
