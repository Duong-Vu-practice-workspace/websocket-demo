package vn.edu.ptit.duongvct.demo.websocket_demo.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
    private int statusCode;
    private String status;
    private Object message;
    private T data;
    private String error;
    private LocalDateTime timestamp;

    public ApiResponse(HttpStatus httpStatus, Object message, T data, String error) {
        this.status = httpStatus.is2xxSuccessful() ? "success" : "error";
        this.statusCode = httpStatus.value();
        this.message = message;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }
}