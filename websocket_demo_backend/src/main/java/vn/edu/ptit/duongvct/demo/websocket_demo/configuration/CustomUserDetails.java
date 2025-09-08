package vn.edu.ptit.duongvct.demo.websocket_demo.configuration;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.User;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.UserService;


import java.util.Collections;

@Component("userDetailsService")
public class CustomUserDetails implements UserDetailsService {
    private final UserService userService;

    public CustomUserDetails(UserService userService) {
        this.userService = userService;

    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Username/password is not valid");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }


}
