package org.ject.recreation.core.api.controller.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorType;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class SessionUserInfoArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SessionUserInfo.class)
                && parameter.getParameterType().equals(SessionUserInfoDto.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
        HttpServletRequest request =
                webRequest.getNativeRequest(HttpServletRequest.class);

        SessionUserInfo annotation =
                parameter.getParameterAnnotation(SessionUserInfo.class);

        boolean required = annotation == null || annotation.required();

        if (request == null) {
            if (required) {
                throw new CoreException(ErrorType.UNAUTHORIZED);
            }
            return null;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            if (required) {
                throw new CoreException(ErrorType.UNAUTHORIZED);
            }
            return null;
        }

        SessionUserInfoDto userInfo =
                (SessionUserInfoDto) session.getAttribute("userInfo");

        if (userInfo == null && required) {
            throw new CoreException(ErrorType.UNAUTHORIZED);
        }

        return userInfo; // required=false 인 경우 null 허용

    }
}