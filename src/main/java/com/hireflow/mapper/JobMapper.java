package com.hireflow.mapper;

import com.hireflow.domain.Job;
import com.hireflow.domain.JobSkill;
import com.hireflow.dto.request.CreateJobRequest;
import com.hireflow.dto.response.JobResponse;
import com.hireflow.dto.response.JobSkillResponse;
import com.hireflow.dto.response.JobSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Job entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "companyName", source = "recruiter.companyName")
    @Mapping(target = "recruiterId", source = "recruiter.id")
    @Mapping(target = "skills", source = "jobSkills", qualifiedByName = "mapJobSkills")
    JobResponse toJobResponse(Job job);

    @Mapping(target = "companyName", source = "recruiter.companyName")
    @Mapping(target = "skills", source = "jobSkills", qualifiedByName = "mapJobSkillNames")
    JobSummaryResponse toJobSummaryResponse(Job job);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recruiter", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "jobSkills", ignore = true)
    Job toJob(CreateJobRequest request);

    @Named("mapJobSkills")
    default List<JobSkillResponse> mapJobSkills(List<JobSkill> jobSkills) {
        if (jobSkills == null) return List.of();
        return jobSkills.stream()
                .map(js -> new JobSkillResponse(js.getSkill().getName(), js.isRequired()))
                .collect(Collectors.toList());
    }

    @Named("mapJobSkillNames")
    default List<String> mapJobSkillNames(List<JobSkill> jobSkills) {
        if (jobSkills == null) return List.of();
        return jobSkills.stream()
                .map(js -> js.getSkill().getName())
                .collect(Collectors.toList());
    }
}
