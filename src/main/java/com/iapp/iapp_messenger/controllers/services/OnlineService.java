package com.iapp.iapp_messenger.controllers.services;

import com.iapp.iapp_messenger.dao.hibernate.User;
import com.iapp.iapp_messenger.dao.hibernate.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
public class OnlineService {

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;

    public OnlineService(StringRedisTemplate redis, UserRepository userRepository) {
        this.redis = redis;
        this.userRepository = userRepository;
    }

    // TODO windows!!!
    /*
    @PostConstruct
    public void test() {
        redis.opsForValue().set("test", "123");
    }
    */

    /**
     * Все новые сесси должны быть переданы в этот метод для корректной работы методов isOnline & getSessions
     * */
    public void addSession(String login, String sessionId) {
        redis.opsForSet().add(key(login), sessionId);
    }

    /**
     * Все WS соединения, которые ЯВНО (передали DISCONNECT) отключиличь
     * должны быть преданы в этотм метод для корректной работы isOnline & getSessions.
     * */
    public void removeSession(String login, String sessionId) {

        // ВАЖНОЕ ЗАМЕЧАНИЕ:
        // WSConfig должен быть heartbeat, чтобы не было скрытый "мертвых" соединений,
        // которые не отвечает за ping-pong.

        // см. WebSocketConfig
        //heartbeat timeout
        //-> Spring closes WS
        //-> SessionDisconnectEvent
        //-> removeSession()
        //-> lastSeen updated

        redis.opsForSet().remove(key(login), sessionId);

        Long size = redis.opsForSet().size(key(login));

        // ВСЕ устройства offline
        if (size == null || size == 0) {

            redis.delete(key(login));

            User user = userRepository.findByLogin(login).orElse(null);

            if (user != null) {
                user.setLastSeen(Instant.now());
                userRepository.save(user);
            }
        }
    }

    public boolean isOnline(String login) {
        Long size = redis.opsForSet().size(key(login));
        return size != null && size > 0;
    }

    /**
     * @return информации о всех авторизованных сессиях (т.е. логин и id сессии)
     * Один логин может быть у разных сессий (несколько устройств)
     * */
    public Set<String> getSessions(String login) {
        return redis.opsForSet().members(key(login));
    }

    private String key(String login) {
        return "ws:user:" + login + ":sessions";
    }
}

