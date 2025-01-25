package me.cho.snackball.study.comment.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.user.domain.User;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor @NoArgsConstructor
public class CreateStudyCommentForm extends BaseUserEntity {

    @NotBlank
    @Length(max = 250)
    private String content;
}
