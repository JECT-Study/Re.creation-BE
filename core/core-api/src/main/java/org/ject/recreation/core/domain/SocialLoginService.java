package org.ject.recreation.core.domain;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.request.SocialLoginRequestDto;
import org.ject.recreation.core.api.controller.response.SocialLoginResponseDto;
import org.ject.recreation.storage.db.core.UserEntity;
import org.ject.recreation.storage.db.core.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialLoginService {
    private final UserRepository userRepository;
    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String kakaoClientId;
    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;
    @Value("${kakao.client-secret:}")
    private String kakaoClientSecret;

    public SocialLoginResponseDto loginWithKakao(SocialLoginRequestDto request) {
        String accessToken = getKakaoAccessToken(request.getCode());
        Map<String, String> userInfo = getKakaoUserInfo(accessToken);
        UserEntity userEntity = saveOrUpdateUser(userInfo);
        return createResponse(userEntity);
    }

    private String getKakaoAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("grant_type=authorization_code")
                .append("&client_id=").append(kakaoClientId)
                .append("&redirect_uri=").append(kakaoRedirectUri)
                .append("&code=").append(code);
        if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
            bodyBuilder.append("&client_secret=").append(kakaoClientSecret);
        }
        String body = bodyBuilder.toString();
        Mono<Map> tokenMono = webClient.post()
            .uri(tokenUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class);
        Map tokenResponse = tokenMono.block();
        return (String) tokenResponse.get("access_token");
    }

    private Map<String, String> getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        Mono<Map> userMono = webClient.get()
            .uri(userInfoUrl)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class);
        Map userResponse = userMono.block();

        Map kakaoAccount = (Map) userResponse.get("kakao_account");
        Map profile = (Map) kakaoAccount.get("profile");
        
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", (String) kakaoAccount.get("email"));
        userInfo.put("nickname", (String) profile.get("nickname"));
        userInfo.put("profileImageUrl", (String) profile.get("profile_image_url"));
        
        return userInfo;
    }

    private UserEntity saveOrUpdateUser(Map<String, String> userInfo) {
        String email = userInfo.get("email");
        String nickname = userInfo.get("nickname");
        String profileImageUrl = userInfo.get("profileImageUrl");

        UserEntity userEntity = userRepository.findById(email).orElse(
            new UserEntity(email, "kakao", profileImageUrl, nickname, LocalDateTime.now(), LocalDateTime.now())
        );
        return userRepository.save(userEntity);
    }

    private SocialLoginResponseDto createResponse(UserEntity userEntity) {
        return SocialLoginResponseDto.builder()
            .email(userEntity.getEmail())
            .nickname(userEntity.getNickname())
            .profileImageUrl(userEntity.getProfileImageUrl()).build();
    }
} 