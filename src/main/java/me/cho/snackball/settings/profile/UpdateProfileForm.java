package me.cho.snackball.settings.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class UpdateProfileForm {

    @NotBlank
    @Length(min = 3, max = 10)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{3,10}$")
    private String nickname;

    @Length(max = 50)
    private String occupation;

    @Length(max = 50)
    private String company;

    @Length(max = 50)
    private String url;

    @Length(max = 50)
    private String description;

    private String profileImage;
}
