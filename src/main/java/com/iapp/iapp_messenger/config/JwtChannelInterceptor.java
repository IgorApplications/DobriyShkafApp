package com.iapp.iapp_messenger.config;

import com.iapp.iapp_messenger.controllers.services.JwtService;
import com.iapp.iapp_messenger.dao.hibernate.User;
import com.iapp.iapp_messenger.dao.hibernate.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Это конфиг JWT для Spring Security для WS (см. WebSocketConfig (configureClientInboundChannel))
 * */
/**
 * interceptor = security gate
 * controller = business logic
 * WsResponse = только после подключения
 * */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtChannelInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /*
    Передавать JWT при подключении

    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);

    stompClient.connect({
        Authorization: "Bearer " + token
    }, onConnected);

    Перехватить JWT на сервере:
    * */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // accessor != null ---> проверка на плохие соединения
        if (accessor != null &&  StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                String login = jwtService.extractLogin(token);

                User user = userRepository.findByLogin(login).orElse(null);

                /** Из JWT получаем Account и проверяем валидность JWT (для WS) */
                // jwtService.isValid(token, user) --> проверка JWT на то, что он реальный, сотв.
                if (user != null && jwtService.isValid(token, user)) {
                    // IMPORTANT для WS СИСТЕМЫ!!!
                    accessor.setUser(
                            new UsernamePasswordAuthenticationToken(user, null, List.of())
                    );
                } else {
                    /**Это жизнь до WSController, жизнь до WsResponse (low level)*/
                    /**
                     * Клиент получит:
                     * ERROR
                     * message:Failed to send message
                     * Invalid JWT
                     * ERROR frame
                     * */
                    throw new IllegalArgumentException("Invalid JWT");
                }
            }
        }

        return message;
    }
}