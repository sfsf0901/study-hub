package me.cho.snackball.study.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
public class CreateStudyForm {

/*    @NotBlank
    @Length(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9]{2,20}$")
    private String path;*/

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


}
