package me.cho.snackball.study.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.study.domain.Study;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
public class UpdateStudyForm {

    private Long studyId;

    @NotBlank
    @Length(max = 30)
    private String title;

    private List<String> studyTags;

    private List<String> locations;

    @NotBlank
    private String fullDescription;

    @NotBlank
    @Length(max = 1000)
    private int limitOfEnrollment;


    public UpdateStudyForm(Study study, List<String> studyStudyTagNames, List<String> studyLocationNames) {
        this.studyId = study.getId();
        this.title = study.getTitle();
        this.studyTags = studyStudyTagNames;
        this.locations = studyLocationNames;
        this.fullDescription = study.getFullDescription();
        this.limitOfEnrollment = study.getLimitOfEnrollment();
    }

}
