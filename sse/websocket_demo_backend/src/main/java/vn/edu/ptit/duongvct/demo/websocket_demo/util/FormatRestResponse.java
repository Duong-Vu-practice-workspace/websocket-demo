package vn.edu.ptit.duongvct.demo.websocket_demo.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.response.ApiResponse;
import vn.edu.ptit.duongvct.demo.websocket_demo.util.annotation.ApiMessage;

@ControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/actuator")) {
            return body;
        }
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();
        String apiMessage = new String();
        if (body instanceof String || body instanceof Resource) {
            return body;
        }

        if (status >= 400) {
            return body;
        } else {
            ApiMessage message = returnType.getMethodAnnotation(ApiMessage.class);
            apiMessage = message != null ? message.value() : "CALL API SUCCESS";
        }
        ApiResponse<Object> res = new ApiResponse<>(HttpStatus.valueOf(status), apiMessage, body, null);
        return res;
    }
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }
}
