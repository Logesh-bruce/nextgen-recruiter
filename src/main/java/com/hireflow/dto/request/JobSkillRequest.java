package com.hireflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillRequest {

    @NotBlank(message = "Skill name must not be blank")
    private String name;

    @Builder.Default
    private boolean required = true;
}
