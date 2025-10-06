package vn.edu.ptit.duongvct.demo.websocket_demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.response.ApiResponse;
@RestControllerAdvice
public class GlobalExceptionHandler {
    //handle all exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllException(HttpServletRequest request, Exception exception) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            // For SSE clients send plain text (avoid trying to serialize ApiResponse to text/event-stream)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("internal server error");
        }

        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setError("Internal Server Error");
        response.setMessage(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

}
