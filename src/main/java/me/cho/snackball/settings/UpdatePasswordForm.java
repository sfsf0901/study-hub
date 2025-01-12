package me.cho.snackball.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.domain.User;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class UpdatePasswordForm {

    @Length(min = 8, max = 50)
    private String password;

    @Length(min = 8, max = 50)
    private String confirmPassword;
}
