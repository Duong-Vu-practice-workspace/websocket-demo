package vn.edu.ptit.duongvct.demo.websocket_demo.dto.response.user;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCreateUserDTO {
    private Long id;
    private String name;
    private String email;

}
