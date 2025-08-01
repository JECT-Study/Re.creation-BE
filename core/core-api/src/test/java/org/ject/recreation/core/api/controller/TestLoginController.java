package org.ject.recreation.core.api.controller;

import jakarta.servlet.http.HttpSession;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.support.response.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Profile("local")
@RestController
public class TestLoginController {
    @PostMapping("/test/login/kakao")
    public ApiResponse<Void> mockLogin(HttpSession session) {
        SessionUserInfoDto userInfo = SessionUserInfoDto.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .profileImageUrl("http://image.url/question")
                .build();

        session.setAttribute("userInfo", userInfo);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userInfo, null, List.of(new SimpleGrantedAuthority("USER"))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return ApiResponse.success(null);
    }
}
