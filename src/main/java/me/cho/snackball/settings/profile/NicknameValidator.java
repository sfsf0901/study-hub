package me.cho.snackball.settings.profile;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.user.domain.User;
import me.cho.snackball.user.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateProfileForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UpdateProfileForm form = (UpdateProfileForm) target;

        User user = userRepository.findByNickname(form.getNickname()).orElse(null);

        if (user != null && !user.getNickname().equals(form.getNickname())) {
            errors.rejectValue("nickname", null, "이미 사용중인 닉네임입니다.");
        }
    }
}
