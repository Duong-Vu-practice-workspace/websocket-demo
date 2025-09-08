package vn.edu.ptit.duongvct.demo.websocket_demo.util;

import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import vn.edu.ptit.duongvct.demo.websocket_demo.dto.response.auth.ResponseLoginDTO;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class SecurityUtil {
    private final JwtEncoder jwtEncoder;
    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${jwt.base64-secret}")
    private String jwtKey;

    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;


    public String createAccessToken(String email, ResponseLoginDTO resLoginDTO) {
        ResponseLoginDTO.UserInsideToken userInsideToken = new ResponseLoginDTO.UserInsideToken();
        userInsideToken.setUsername(resLoginDTO.getUser().getUsername());
        userInsideToken.setId(resLoginDTO.getUser().getId());
        userInsideToken.setFullName(resLoginDTO.getUser().getFullName());
        userInsideToken.setRole(resLoginDTO.getUser().getRole());

        Instant nowTime = Instant.now();
        Instant validity = nowTime.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);
        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(nowTime)
                .expiresAt(validity)
                .subject(email)
                .claim("user", userInsideToken)
                .claim("permission", userInsideToken.getRole())

                .build();
        JwsHeader jwsHeader = JwsHeader .with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader ,claims)).getTokenValue();
    }

    //decode jwk
    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(
                keyBytes,
                0,
                keyBytes.length,
                JWT_ALGORITHM.getName()
        );
    }


    public String createRefreshToken(String email, ResponseLoginDTO dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        ResponseLoginDTO.UserInsideToken userToken = new ResponseLoginDTO.UserInsideToken();
        userToken.setId(dto.getUser().getId());
        userToken.setUsername(dto.getUser().getUsername());
        userToken.setFullName(dto.getUser().getFullName());

        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", userToken)
                .build();
        JwsHeader jwsHeader = JwsHeader .with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader ,claims)).getTokenValue();
    }
    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user.
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String) authentication.getCredentials());
    }

    public Jwt checkValidRefreshToken(String refreshToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        try {
            return   jwtDecoder.decode(refreshToken);

        } catch (Exception e) {
            System.out.println(">>> Refresh Token error: " + e.getMessage());
            throw e;
        }
    }

}