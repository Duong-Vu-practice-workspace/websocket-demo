package vn.edu.ptit.duongvct.demo.websocket_demo.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.response.ApiResponse;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private final ObjectMapper mapper;

    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        this.delegate.commence(request, response, authException);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Object> res = new ApiResponse<Object>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        String errorMessage = Optional.ofNullable(authException.getCause()) // NULL
                .map(Throwable::getMessage)
                .orElse(authException.getMessage());
        res.setError(errorMessage);

        // res.setError(authException.getCause().getMessage());

        res.setMessage("Token is not valid(expired, not in correct format or not pass Jwt in header)...");

        mapper.writeValue(response.getWriter(), res);

    }
}