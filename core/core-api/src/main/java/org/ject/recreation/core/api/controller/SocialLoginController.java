package org.ject.recreation.core.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.request.SocialLoginRequestDto;
import org.ject.recreation.core.api.controller.response.SocialLoginResponseDto;
import org.ject.recreation.core.domain.SocialLoginService;
import org.ject.recreation.core.support.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class SocialLoginController {
    private final SocialLoginService socialLoginService;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @PostMapping("/login/kakao")
    public ApiResponse<SocialLoginResponseDto> login(@RequestBody SocialLoginRequestDto request, HttpSession session) {
        SocialLoginResponseDto response = socialLoginService.loginWithKakao(request);
        if (response.getEmail() != null) {
            session.setAttribute("userEmail", response.getEmail());
        }
        return ApiResponse.success(response);
    }

    // 로그인 테스트 api
    @GetMapping("/login/kakao/test")
    public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&response_type=code";
        response.sendRedirect(url);
    }
} 