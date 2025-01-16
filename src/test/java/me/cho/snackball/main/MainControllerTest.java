package me.cho.snackball.main;

import me.cho.snackball.user.UserRepository;
import me.cho.snackball.user.UserService;
import me.cho.snackball.user.dto.SignupForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        SignupForm signupForm = new SignupForm();
        signupForm.setUsername("testuser@test.com");
        signupForm.setNickname("testuser");
        signupForm.setPassword("12345678");
        userService.processNewAccount(signupForm);
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 페이지 이동 테스트")
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login")) // 로그인 페이지 호출
                .andExpect(status().isOk())
                .andExpect(view().name("user/login")) // 반환된 뷰 이름 확인
                .andExpect(content().string(containsString("<form"))); // HTML 폼 태그 확인
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "testuser@test.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection()) // 리다이렉트 확인
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("testuser@test.com")); // 성공 후 리다이렉트 URL 확인
    }

    @Test
    @DisplayName("로그인 실패 테스트")
    void testLoginFailure() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "wrongpassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}