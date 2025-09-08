package vn.edu.ptit.duongvct.demo.websocket_demo.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    private String fullName;
    private String password;
    private String role;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;
}
