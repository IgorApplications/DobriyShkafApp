package com.iapp.iapp_messenger.controllers.general_ws;

import com.iapp.iapp_messenger.config.JwtChannelInterceptor;
import com.iapp.iapp_messenger.controllers.services.OnlineService;
import com.iapp.iapp_messenger.dao.dto.PresenceEvent;
import com.iapp.iapp_messenger.dao.hibernate.User;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

/**
 * Оповещает всех залогиненных пользоватлей через /topic внутри WS соединения о том,
 * что какой-либо пользователь изменил свой онлайн статус.
 * client.subscribe("/topic/presence", frame => {
 *     const event = JSON.parse(frame.body);
 *     console.log(event.login);
 *     console.log(event.online);
 * });
 * */
@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final OnlineService onlineService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, OnlineService onlineService) {
        this.messagingTemplate = messagingTemplate;
        this.onlineService = onlineService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        Optional<UserData> opUserAndId = getUser(event);
        if (opUserAndId.isEmpty()) return;

        UserData userAndId = opUserAndId.get();
        boolean wasOffline = !onlineService.isOnline(userAndId.user.getLogin());

        // Нужно передавать все данные для корректной работы онлайн статуса
        onlineService.addSession(userAndId.user.getLogin(), userAndId.sessionId);

        // FIRST DEVICE ONLINE
        if (wasOffline) {
            messagingTemplate.convertAndSend(
                    "/topic/presence",
                    new PresenceEvent(
                            userAndId.user.getLogin(),
                            true
                    )
            );
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Optional<UserData> opUserAndId = getUser(event);
        if (opUserAndId.isEmpty()) return;
        UserData userAndId = opUserAndId.get();

        // Нужно передавать все данные для корректной работы онлайн статуса
        onlineService.removeSession(userAndId.user.getLogin(), userAndId.sessionId);

        // ALL DEVICES OFFLINE
        if (!onlineService.isOnline(userAndId.user.getLogin())) {
            messagingTemplate.convertAndSend(
                    "/topic/presence",
                    new PresenceEvent(
                            userAndId.user.getLogin(),
                            false
                    )
            );
        }
    }

    /** @see JwtChannelInterceptor */
    private Optional<UserData> getUser(AbstractSubProtocolEvent event) {
        // accessor.getLogin(); -> STOMP LOGIN, не из JWT (обычно NULL)
        /*
         * CONNECT event может прийти:
         * ДО interceptor
         * или при ошибке
         * */
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof Authentication auth) {
            // Login из JWT
            return Optional.of(new UserData((User) auth.getPrincipal(), accessor.getSessionId()));
        }
        return Optional.empty();
    }

    private record UserData(User user, String sessionId) {}
}

/**
 * 1. Online/offline статус
 * userService.setOnline(user.getId(), true);
 * 2. Логирование
 * Connected: user1
 * Disconnected: user1
 * 3. Presence система (как в Telegram)
 * кто онлайн
 * кто оффлайн
 * 4. Очистка ресурсов
 * удалить из активных сессий
 * убрать subscriptions
 * */


