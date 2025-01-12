package me.cho.snackball.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.domain.User;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;

@Data
public class UpdateNotificationsForm {

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;
}
