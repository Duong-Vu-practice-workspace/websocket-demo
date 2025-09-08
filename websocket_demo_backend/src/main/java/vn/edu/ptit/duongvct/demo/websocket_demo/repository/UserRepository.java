package vn.edu.ptit.duongvct.demo.websocket_demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshTokenAndEmail(String refreshToken, String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
