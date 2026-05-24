package com.iapp.iapp_messenger.controllers.rest_controllers;

import com.iapp.iapp_messenger.dao.dto.ApiResponse;
import com.iapp.iapp_messenger.dao.hibernate.PersonalMessage;
import com.iapp.iapp_messenger.dao.hibernate.PersonalMessageRepository;
import com.iapp.iapp_messenger.dao.hibernate.User;
import com.iapp.iapp_messenger.dao.hibernate.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller для личных сообщений
 * */
@RestController
@RequestMapping("/api/messages")
public class RESTPersonalMessageController {

    private final UserRepository userRepository;
    private final PersonalMessageRepository personalMessageRepository;

    public RESTPersonalMessageController(UserRepository userRepository, PersonalMessageRepository personalMessageRepository) {
        this.userRepository = userRepository;
        this.personalMessageRepository = personalMessageRepository;
    }

    /**
     * Поллучение истори сообщений, последние x сообщений, где x = size.
     * Парметр page отвчает за сдвиг на x сообдений в конце.
     * Пример: GET /api/messages/dialog/vasya?page=0&size=20
     * */
    @GetMapping("/dialog/{login}")
    public ApiResponse<List<PersonalMessage>> getDialog(@PathVariable String recipientLogin,
                                                        @RequestParam int page,
                                                        @RequestParam int size,
                                                        Authentication auth) {
        User sender = (User) auth.getPrincipal();
        User recipient = userRepository.findByLogin(recipientLogin).orElseThrow();

        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.ok(personalMessageRepository.findDialog(
                sender.getId(),
                recipient.getId(),
                pageable));
    }
}
