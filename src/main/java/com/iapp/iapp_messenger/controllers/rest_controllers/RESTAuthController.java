package com.iapp.iapp_messenger.controllers.rest_controllers;

import com.iapp.iapp_messenger.controllers.services.JwtService;
import com.iapp.iapp_messenger.controllers.services.UserService;
import com.iapp.iapp_messenger.dao.dto.ApiResponse;
import com.iapp.iapp_messenger.dao.dto.AuthRequest;
import com.iapp.iapp_messenger.dao.hibernate.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Авторизация и регистраций пользователя
 * */
@RestController
@RequestMapping("/api/auth")
public class RESTAuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public RESTAuthController(UserService userService,
                              JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Проверяет синтаксическую корректность почты.
     * */
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody AuthRequest request) {
        User user = userService.register(
                request.getLogin(),
                request.getPassword(),
                request.getEmail()
        );

        String token = jwtService.generateToken(user);

        return ApiResponse.ok(token);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody AuthRequest request) {
        User user = userService.login(
                request.getLogin(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user);
        return ApiResponse.ok(token);
    }


    @GetMapping("/hi")
    public String test() {
        return "HELLO WORLD!!! ITS LOLCHAT!!!";
    }
}