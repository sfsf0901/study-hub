package me.cho.snackball.user;

import jakarta.mail.internet.MimeMessage;
import me.cho.snackball.user.domain.Authority;
import me.cho.snackball.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc //필터, 시큐리티 포함
@ActiveProfiles("test")
//@WebMvcTest(AccountController.class) //필터, 시큐리티 미포함, 컨트롤러 계층만 로드
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    //Spring Boot 3.4.0부터 @MockBean과 @MockBeans는 더 이상 사용되지 않으며,
    // 대신 @MockitoBean과 @MockitoSpyBean이 도입되었습니다.
    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("GET /sign-up - 회원가입 폼")
    void signUpFormTest() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk()) // HTTP 상태 코드 200 확인
                .andExpect(view().name("user/signup"));
//                .andExpect(model().attributeExists("signUpForm")); // 반환된 뷰 이름 확인
    }

    @Test
    @DisplayName("POST /signup - 회원 가입 입력값 오류 검증")
    void signUpFormValidationErrorTest() throws Exception {
        // 누락되거나 잘못된 입력값을 포함한 요청 시뮬레이션
        mockMvc.perform(post("/signup")
                        .param("nickname", "") // 빈 값 (필수 필드 누락)
                        .param("username", "invalid-email") // 잘못된 이메일 형식
                        .param("password", "123") // 너무 짧은 비밀번호
                        .with(csrf()))
                .andExpect(status().isOk()) // HTTP 상태는 200이어야 함 (양식 다시 렌더링)
                .andExpect(view().name("user/signup")); // 회원 가입 폼 다시 렌더링 확인
//                .andExpect(model().attributeHasFieldErrors("signUpForm", "nickname", "username", "password")); // 특정 필드에 검증 오류 존재 확인
    }

    @Test
    @DisplayName("POST /signup - 회원 가입 입력값 정상")
    void signUpSubmitTest() throws Exception {
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);

        // 누락되거나 잘못된 입력값을 포함한 요청 시뮬레이션
        mockMvc.perform(post("/signup")
                        .param("nickname", "cho") // 빈 값 (필수 필드 누락)
                        .param("username", "cho@test.com") // 잘못된 이메일 형식
                        .param("password", "12345678") // 너무 짧은 비밀번호
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        User user = userRepository.findByUsername("cho@test.com").orElse(null);
        assertNotNull(user);
        assertNotEquals("12345678", user.getPassword());

        assertTrue(userRepository.existsByUsername("cho@test.com"));
        then(javaMailSender).should().send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("GET /checkemailtoken - 입력값 오류")
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/checkemailtoken")
                .param("token", "asdfadsfd")
                .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("user/checkedEmail"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("GET /checkemailtoken - 입력값 정상")
    void checkEmailToken() throws Exception {
        User user = new User();
        user.setUsername("test@email.com");
        user.setPassword("12345678");
        user.setNickname("testuser");
        user.setAuthority(Authority.ROLE_USER);
        User newUser = userRepository.save(user);
        newUser.genToken();

        mockMvc.perform(get("/checkemailtoken")
                .param("token", newUser.getEmailCheckToken())
                .param("email", newUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("user/checkedEmail"))
                .andExpect(authenticated());
    }
}