package com.user.manage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.manage.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;

    @Test
    @DisplayName("⑩ 정상 회원가입 요청 시 201 반환")
    void signup_shouldReturn201() throws Exception {
        willDoNothing().given(authService).signUp(any());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "user@example.com", "password", "password123"))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("⑪ 잘못된 이메일 형식으로 회원가입 시 400 반환")
    void signup_shouldReturn400_onInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "not-an-email", "password", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("⑫ Authorization 헤더 없이 /api/user/me 접근 시 401 반환")
    void me_shouldReturn401_withoutToken() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }
}
