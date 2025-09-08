package vn.edu.ptit.duongvct.demo.websocket_demo.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.User;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.request.auth.RequestLoginDTO;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.request.auth.RequestRegisterUserDTO;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.response.auth.ResponseLoginDTO;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.response.auth.ResponseRegisterDTO;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.UserService;
import vn.edu.ptit.duongvct.demo.websocket_demo.util.SecurityUtil;
import vn.edu.ptit.duongvct.demo.websocket_demo.util.annotation.ApiMessage;

import java.util.Optional;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/register")
    @ApiMessage("Register a user")
    public ResponseEntity<ResponseRegisterDTO> register(@Valid @RequestBody RequestRegisterUserDTO dto) throws IllegalArgumentException {
        if (this.userService.isEmailExists(dto.getEmail())) {
            throw new IllegalArgumentException("Email " + dto.getEmail() + " is already exists. Please choose another email");
        }
        String hashedPassword = this.passwordEncoder.encode(dto.getPassword());
        dto.setPassword(hashedPassword);
        User createdUser = this.userService.createUser(this.userService.mapRequestRegisterDTO(dto));
        ResponseRegisterDTO registerDTO = this.userService.mapUser(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerDTO);
    }
    @PostMapping("/login")
    @ApiMessage("Login")
    public ResponseEntity<ResponseLoginDTO> login(@Valid @RequestBody RequestLoginDTO requestLoginDTO) {
        try {
            //take input(username and password) into security
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(requestLoginDTO.getUsername(), requestLoginDTO.getPassword());

            //authenticate user (override UserDetailsService bean)
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(usernamePasswordAuthenticationToken);

            //push information if success to securitycontext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //not pass password
            ResponseLoginDTO responseLoginDTO = new ResponseLoginDTO();
            Optional<User> currentUser = this.userService.findByUsername(requestLoginDTO.getUsername());
            if (currentUser.isPresent()) {
                ResponseLoginDTO.UserLogin userLogin = new ResponseLoginDTO.UserLogin(
                        currentUser.get().getId(),
                        currentUser.get().getUsername(),
                        currentUser.get().getFullName(),
                        currentUser.get().getRole()
                );
                responseLoginDTO.setUser(userLogin);
            }
            String accessToken = this.securityUtil.createAccessToken(requestLoginDTO.getUsername(), responseLoginDTO);
            responseLoginDTO.setAccessToken(accessToken);

            String refreshToken = this.securityUtil.createRefreshToken(requestLoginDTO.getUsername(), responseLoginDTO);

            this.userService.updateUserToken(refreshToken, requestLoginDTO.getUsername());

            //set cookies
            ResponseCookie resCookies = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                    .body(responseLoginDTO);
        } catch (BadCredentialsException | UsernameNotFoundException | InternalAuthenticationServiceException e) {
            throw new BadCredentialsException("Incorrect username or password");
        }


    }
    @GetMapping("/account")
    @ApiMessage("Fetch account")
    public ResponseEntity<ResponseLoginDTO.UserGetAccount> getAccount() {

        String username = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        // Lấy user
        Optional<User> currentUserDB = this.userService.findByUsername(username);
        ResponseLoginDTO.UserLogin userLogin = new ResponseLoginDTO.UserLogin();
        ResponseLoginDTO.UserGetAccount userGetAccount = new ResponseLoginDTO.UserGetAccount();

        if (currentUserDB.isPresent()) {
            userLogin.setId(currentUserDB.get().getId());
            userLogin.setUsername(currentUserDB.get().getUsername());
            userLogin.setFullName(currentUserDB.get().getFullName());
            userLogin.setRole(currentUserDB.get().getRole());
            userGetAccount.setUser(userLogin);
        }

        return ResponseEntity.ok().body(userGetAccount);
    }
    @GetMapping("/refresh")
    @ApiMessage("Get User by refresh token")
    public ResponseEntity<ResponseLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "defaultTokenValue") String refreshToken) throws IllegalArgumentException {
        if (refreshToken.equals("defaultTokenValue")) {
            throw new IllegalArgumentException("You don't have refresh token in cookies");
        }

        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String username = decodedToken.getSubject();

        // check user by token + email
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, username);
        if (currentUser == null) {
            throw new IllegalArgumentException("Refresh Token not valid");
        }

        // issue new token/set refresh token as cookies
        ResponseLoginDTO res = new ResponseLoginDTO();
        Optional<User> currentUserDB = this.userService.findByUsername(username);
        if (currentUserDB.isPresent()) {

            ResponseLoginDTO.UserLogin userLogin = new ResponseLoginDTO.UserLogin(
                    currentUserDB.get().getId(),
                    currentUserDB.get().getUsername(),
                    currentUserDB.get().getFullName(),
                    currentUserDB.get().getRole()
            );
            res.setUser(userLogin);
        }

        // create access_token token
        String accessToken = this.securityUtil.createAccessToken(username, res);

        res.setAccessToken(accessToken);

        // create refresh token
        String newRefreshToken = this.securityUtil.createRefreshToken(username, res);

        // Update user with new_refresh_token
        this.userService.updateUserToken(newRefreshToken, username);

        // set cookies
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @PostMapping("/logout")
    @ApiMessage("Logout User")
    public ResponseEntity<Void> logout() throws IllegalArgumentException {
        String username = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if (this.userService.findByUsername(username).isEmpty()) {
            throw new IllegalArgumentException("Access Token is not valid");
        }

        // Update refresh token = null
        this.userService.updateUserToken(null, username);

        // remove refresh token cookie
        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(null);
    }
}