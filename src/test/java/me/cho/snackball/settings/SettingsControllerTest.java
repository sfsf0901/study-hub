package me.cho.snackball.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.cho.snackball.WithUser;
import me.cho.snackball.domain.StudyTag;
import me.cho.snackball.domain.User;
import me.cho.snackball.domain.UserStudyTag;
import me.cho.snackball.repository.UserRepository;
import me.cho.snackball.studyTag.StudyTagRepository;
import me.cho.snackball.studyTag.UserStudyTagRepository;
import me.cho.snackball.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private StudyTagRepository studyTagRepository;

    @Autowired
    private UserStudyTagRepository userStudyTagRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;



    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @WithUser("testuser@test.com")
    @DisplayName("GET /settings/profile - 프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.URL_SETTINGS_PROFILE))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("updateProfileForm"));
    }

    @WithUser("testuser@test.com")
    @DisplayName("POST /settings/profile - 프로필 수정 입력값 정상")
    @Test
    void updateProfileTest() throws Exception {
        String description = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post(SettingsController.URL_SETTINGS_PROFILE)
                        .param("nickname", "testuser")
                        .param("description", description)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.URL_SETTINGS_PROFILE))
                .andExpect(flash().attributeExists("message"));

        User user = userRepository.findByNickname("testuser").get();
        assertEquals(description, user.getDescription());
    }

    @WithUser("testuser@test.com")
    @DisplayName("POST /settings/profile - 프로필 수정 입력값 오류 검증")
    @Test
    void updateProfileErrorTest() throws Exception {
        String description = "짧은 소개 50자 이상. 짧은 소개 50자 이상. 짧은 소개 50자 이상. 짧은 소개 50자 이상.";
        mockMvc.perform(post(SettingsController.URL_SETTINGS_PROFILE)
                        .param("nickname", "testuser")
                        .param("description", description)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.VIEW_SETTINGS_PROFILE))
                .andExpect(model().attributeExists("updateProfileForm"))
                .andExpect(model().hasErrors());

        User user = userRepository.findByNickname("testuser").get();
        assertNull(user.getDescription());
    }

    @WithUser("testuser@test.com")
    @DisplayName("GET /settings/password - 비밀번호 수정 폼")
    @Test
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingsController.URL_SETTINGS_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().attributeExists("updatePasswordForm"));
    }

    @WithUser("testuser@test.com")
    @DisplayName("POST /settings/password - 비밀번호 변경 입력값 정상")
    @Test
    void updatePasswordTest() throws Exception {
        mockMvc.perform(post(SettingsController.URL_SETTINGS_PASSWORD)
                        .param("password", "11111111")
                        .param("confirmPassword", "11111111")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.URL_SETTINGS_PASSWORD))
                .andExpect(flash().attributeExists("message"));

        User user = userRepository.findByNickname("testuser").get();
        assertTrue(passwordEncoder.matches("11111111", user.getPassword()));
    }

    @WithUser("testuser@test.com")
    @DisplayName("POST /settings/password - 비밀번호 변경 입력값 오류 검증 - 비밀번호 불일치")
    @Test
    void updatePasswordErrorTest() throws Exception {
        mockMvc.perform(post(SettingsController.URL_SETTINGS_PASSWORD)
                        .param("password", "11111111")
                        .param("confirmPassword", "22222222")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.VIEW_SETTINGS_PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("updatePasswordForm"));
    }

    @WithUser("testuser@test.com")
    @DisplayName("GET /settings/studytags - 태그 수정 폼")
    @Test
    void updateStudyTagsForm() throws Exception {
        mockMvc.perform(get(SettingsController.URL_SETTINGS_STUDY_TAGS))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/studyTags"))
                .andExpect(model().attributeExists("studyTags"))
                .andExpect(model().attributeExists("userStudyTags"));
    }

    @WithUser("testuser@test.com")
    @DisplayName("POST /settings/studytags/add - 태그 추가")
    @Test
    void updateStudyTagsAddTest() throws Exception {
        UpdateStudyTagsForm updateStudyTagsForm = new UpdateStudyTagsForm();
        updateStudyTagsForm.setTagName("testTag");

        mockMvc.perform(post(SettingsController.URL_SETTINGS_STUDY_TAGS + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStudyTagsForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        StudyTag studyTag = studyTagRepository.findByName("testTag").orElse(null);
        assertNotNull(studyTag);
        assertNotNull(userRepository.findByUsername("testuser@test.com").orElse(null).getUserStudyTags().contains(studyTag));
    }

    @WithUser("testuser@test.com")
    @DisplayName("POST /settings/studytags/remove - 태그 삭제")
    @Test
    void updateStudyTagsRemoveTest() throws Exception {
        User user = userRepository.findByUsername("testuser@test.com").orElse(null);
        StudyTag studyTag = studyTagRepository.save(StudyTag.builder().name("testTag").build());
        UserStudyTag userStudyTag = UserStudyTag.createUserStudyTag(user, studyTag);
//        userStudyTagRepository.save(userStudyTag);
//        user.getUserStudyTags().add(userStudyTag);

        assertTrue(user.getUserStudyTags().contains(userStudyTag));

        UpdateStudyTagsForm updateStudyTagsForm = new UpdateStudyTagsForm();
        updateStudyTagsForm.setTagName("testTag");

        mockMvc.perform(post(SettingsController.URL_SETTINGS_STUDY_TAGS + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStudyTagsForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(user.getUserStudyTags().contains(studyTag));
    }

}