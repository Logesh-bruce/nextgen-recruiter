package com.hireflow.mapper;

import com.hireflow.domain.Resume;
import com.hireflow.domain.ResumeEducation;
import com.hireflow.domain.ResumeExperience;
import com.hireflow.domain.ResumeSkill;
import com.hireflow.dto.response.ResumeEducationResponse;
import com.hireflow.dto.response.ResumeExperienceResponse;
import com.hireflow.dto.response.ResumeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Resume entity and parsed sections.
 */
@Mapper(componentModel = "spring")
public interface ResumeMapper {

    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    @Mapping(target = "educations", ignore = true)
    ResumeResponse toResumeResponse(Resume resume);

    ResumeExperienceResponse toExperienceResponse(ResumeExperience experience);

    ResumeEducationResponse toEducationResponse(ResumeEducation education);

    default List<String> mapResumeSkills(List<ResumeSkill> resumeSkills) {
        if (resumeSkills == null) return List.of();
        return resumeSkills.stream()
                .map(rs -> rs.getSkill().getName())
                .collect(Collectors.toList());
    }
}
