package me.cho.snackball.settings.password;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class UpdatePasswordForm {

    @Length(min = 8, max = 50)
    private String password;

    @Length(min = 8, max = 50)
    private String confirmPassword;
}
