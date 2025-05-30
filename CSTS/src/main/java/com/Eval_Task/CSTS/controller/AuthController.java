package com.Eval_Task.CSTS.controller;

import com.Eval_Task.CSTS.dto.UserDto;
import com.Eval_Task.CSTS.service.AuthService;
import com.Eval_Task.CSTS.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserDto userDto) {
        return authService.login(userDto);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Object> refresh(@RequestBody Map<String, String> request)
    {
        return authService.refreshToken(request.get("refreshToken"));
    }
}