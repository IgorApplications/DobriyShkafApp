package com.iapp.iapp_messenger.controllers.rest_controllers;

import com.iapp.iapp_messenger.controllers.services.OnlineService;
import com.iapp.iapp_messenger.dao.dto.ApiResponse;
import com.iapp.iapp_messenger.dao.hibernate.PersonalMessageRepository;
import com.iapp.iapp_messenger.dao.hibernate.User;
import com.iapp.iapp_messenger.dao.hibernate.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * REST Controller для аккаунтов
 * */
@RestController
@RequestMapping("/api/accounts")
public class RESTAccountController {

    private final UserRepository userRepository;
    private final OnlineService onlineService;

    public RESTAccountController(UserRepository userRepository, OnlineService onlineService) {
        this.userRepository = userRepository;
        this.onlineService = onlineService;
    }

    /**
     * Проверяет список пользователей на онлайн статус
     * */
    @PostMapping("/online")
    public ApiResponse<Map<String, Boolean>> checkOnline(@RequestBody List<String> logins) {
        Map<String, Boolean> result = new HashMap<>();

        for (String login : logins) {
            result.put(login, onlineService.isOnline(login));
        }

        return ApiResponse.ok(result);
    }

    /**
     * Возвращает последнее время входа в систему у списка пользоватлей.
     * Игнорирует не сущетсвующих пользоватлей.
     * */
    @PostMapping("/last-seen")
    public ApiResponse<Map<String, Instant>> getLastTime(@RequestBody List<String> logins) {
        List<User> users = userRepository.findAllByLogins(logins);

        Map<String, Instant> result = new HashMap<>();
        for (User user : users) {
            result.put(user.getLogin(), user.getLastSeen());
        }

        return ApiResponse.ok(result);
    }

    /**
     * Проверяет сущетсование пользователей по списку логинов
     * */
    @PostMapping("/exists")
    public ApiResponse<Map<String, Boolean>> exists(@RequestBody List<String> logins) {
        List<String> existing = userRepository.findExistingLogins(logins);
        Set<String> existingSet = new HashSet<>(existing);
        Map<String, Boolean> result = new HashMap<>();

        for (String login : logins) {
            result.put(login, existingSet.contains(login));
        }

        return ApiResponse.ok(result);
    }
}
