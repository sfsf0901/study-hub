package me.cho.snackball.studyTag;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateStudyTagsForm {

    @Length(min = 8, max = 50)
    private String tagName;
}
