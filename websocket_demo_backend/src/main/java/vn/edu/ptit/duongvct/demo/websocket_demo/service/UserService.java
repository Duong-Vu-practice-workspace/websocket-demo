package vn.edu.ptit.duongvct.demo.websocket_demo.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.User;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.request.auth.RequestRegisterUserDTO;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.response.auth.ResponseRegisterDTO;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    public UserService(UserRepository userRepository, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public boolean isEmailExists(String email) {
        return this.userRepository.existsByEmail(email);
    }


    public User handleSaveUser(RequestRegisterUserDTO userDTO) {
        User user = this.mapper.map(userDTO,User.class);
        user.setRole("USER");
        return this.userRepository.save(user);
    }
    public User getUserByUsername(String username) {
        return this.userRepository.findByUsername(username).get();
    }
    public void updateUserToken(String token, String email) {
        User currentUser = this.getUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }


    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email).get();
    }
    public Optional<User> findByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }
    public ResponseRegisterDTO mapUser(User user) {
        return mapper.map(user, ResponseRegisterDTO.class);
    }
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists. Please choose another email");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists. Please choose another username");
        }
        user.setRole("USER");
        return userRepository.save(user);
    }
    public User mapRequestRegisterDTO(RequestRegisterUserDTO dto) {
        return mapper.map(dto, User.class);
    }
}
