package me.cho.snackball.study.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.study.domain.Study;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
public class UpdateStudyStatusForm {

    private Long studyId;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    public UpdateStudyStatusForm(Study study) {
        this.studyId = study.getId();
        this.recruiting = study.isRecruiting();
        this.published = study.isPublished();
        this.closed = study.isClosed();
    }

}
