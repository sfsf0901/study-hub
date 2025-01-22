package me.cho.snackball.settings.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.user.domain.User;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

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

//    @Size(max = 5 * 1024 * 1024, message = "파일 크기는 최대 5MB까지 가능합니다.")
//    @Pattern(regexp = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)", message = "이미지 파일만 업로드 가능합니다.")
    private MultipartFile imageFile;

    public UpdateProfileForm(User user) {
        this.nickname = user.getNickname();
        this.occupation = user.getOccupation();
        this.company = user.getCompany();
        this.url = user.getUrl();
        this.description = user.getDescription();
        this.profileImage = user.getProfileImage();
    }
}
