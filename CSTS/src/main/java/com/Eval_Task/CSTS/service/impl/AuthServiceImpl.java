package com.Eval_Task.CSTS.service.impl;

import com.Eval_Task.CSTS.config.JwtGeneratorValidator;
import com.Eval_Task.CSTS.dto.UserDto;
import com.Eval_Task.CSTS.model.Role;
import com.Eval_Task.CSTS.model.User;
import com.Eval_Task.CSTS.repository.RoleRepository;
import com.Eval_Task.CSTS.repository.UserRepository;
import com.Eval_Task.CSTS.service.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private UserRepository userRepo;
    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtGeneratorValidator jwtGenVal;



    @Override
    public ResponseEntity<Object> login(UserDto userDto) {
        try {
            User user = userRepo.findByUsername(userDto.getUsername());
            if (user == null) {
                return generateResponse("Username does not exist", HttpStatus.NOT_FOUND, null);
            }

            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenVal.generateToken(authentication);

            return generateResponse("Login successful", HttpStatus.OK, token);

        } catch (Exception e) {
            return generateResponse("Password is wrong", HttpStatus.UNAUTHORIZED, null);
        }
    }

    @Override
    public ResponseEntity<Object> refreshToken(String refreshToken) {
        try {
            Claims claims = jwtGenVal.extractAllClaims(refreshToken);
            String username = claims.getSubject();

            UserDetails userDetails = loadUserByUsername(username);

            if (!jwtGenVal.isTokenExpired(refreshToken)) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                String newAccessToken = jwtGenVal.generateToken(authentication);
                String newRefreshToken = jwtGenVal.generateRefreshToken(authentication);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", newAccessToken);
                tokens.put("refreshToken", newRefreshToken);

                return ResponseEntity.ok(tokens);
            }

            else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Refresh token expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRole()));
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole()))
                .collect(Collectors.toList());
    }

    private ResponseEntity<Object> generateResponse(String message, HttpStatus st, Object responseobj) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("Status", st.value());
        map.put("data", responseobj);
        return new ResponseEntity<>(map, st);
    }

}