package com.framework.handler;

import com.framework.constants.HttpStatus;
import com.framework.constants.TokenConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author dotm
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(@NonNull HttpServletRequest request,
                         @NonNull HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {
        // 设置响应头
        response.setStatus(HttpStatus.UNAUTHORIZED);
        response.setContentType(TokenConstants.APPLICATION_JSON_CHARSET_UTF_8);
        response.getWriter().print(TokenConstants.CODE_ERROR);
    }

}
