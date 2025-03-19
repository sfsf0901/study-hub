package me.cho.snackball.study.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@AllArgsConstructor @NoArgsConstructor
public class CreateStudyForm {

    @NotBlank
    @Length(max = 30)
    private String title;

    private List<String> studyTags;

    private List<String> locations;

//    @NotBlank
//    @Length(max = 100)
//    private String shortDescription;

    @NotBlank
    private String fullDescription;

    @Min(1)
    @Max(1000)
    private int limitOfEnrollment;
}
