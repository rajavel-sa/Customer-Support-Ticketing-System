package com.Eval_Task.CSTS.service;

import com.Eval_Task.CSTS.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    ResponseEntity<Object> login(UserDto userDto);
    ResponseEntity<Object> refreshToken(String refreshToken);
}