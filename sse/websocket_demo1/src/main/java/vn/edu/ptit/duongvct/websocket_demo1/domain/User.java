package vn.edu.ptit.duongvct.websocket_demo1.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

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
    @OneToMany(mappedBy = "user")
    private List<Backup> backups;
}
