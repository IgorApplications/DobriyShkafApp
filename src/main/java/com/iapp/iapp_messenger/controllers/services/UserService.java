package com.iapp.iapp_messenger.controllers.services;

import com.iapp.iapp_messenger.dao.hibernate.User;
import com.iapp.iapp_messenger.dao.hibernate.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/*
Семантика - читаемость, не поведение
@Component	просто бин
@Service	бизнес-логика
@Repository	DAO (JPA)

DAO:
ENTITY

DTO:
class AuthRequest
class ApiResponse
объекты для передачи данных
не связаны с БД
используются в API
НИКОГДА не отдавай Entity напрямую в API

* */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String login, String password, String email) {
        if (userRepository.existsByLogin(login)) {
            throw new RuntimeException("Login already exists");
        }

        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);

        return userRepository.save(user);
    }

    public User login(String login, String password) {

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        return user;
    }
}
