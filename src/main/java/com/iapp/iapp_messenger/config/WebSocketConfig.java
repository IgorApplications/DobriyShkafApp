package com.iapp.iapp_messenger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

/**
 * websocket configuration
 * @Configuration - one of the main annotations for communication with services
 * @EnableWebSocketMessageBroker - communication with high-level web sockets
 * */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    /**
     * Необходимо для безопасности WS системы (SAFETY_IMPORTANT)
     * */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // Максимальный размер STOMP frame (против больших сообщений)
        registry.setMessageSizeLimit(16 * 1024);

        // Если клиент медленно читает, а сервер ожидает и отправляет сообщения,
        // то накапливается "бесокнечно большая" очередь
        // "если клиент слишком медленный — disconnect"
        registry.setSendBufferSizeLimit(512 * 1024);
        // если отправка слишком долгая → close session
        registry.setSendTimeLimit(15_000);

        /*
            DANGEROUS:
            1. Unlimited connections
                10000 websocket connections
            2. Unlimited message rate
                100000 раз/сек.
            3. Huge frames
                Можно отправить 100MB websocket frame.
         */
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /**
         * /topic - ОТВЕТ ВСЕМ
         * /queue - ПЕРСОНАЛЬНЫЙ ОТВЕТ
         * */

        /*
         Сервер:
            отправляет heartbeat клиенту
            раз в 30 сек.
            И ожидает heartbeat от клиента
            раз в 30 сек.

         Что делает Spring
            SimpleBroker внутри Spring периодически проверяет: lastReadTime, lastWriteTime
            у каждой websocket session.

         Время ожидания вышло:
            Spring закрывает session
                1) Низкоуровнево вызывается:
                WebSocketSession.close()
                2) SessionDisconnectEvent
                3) После этого в нашей системе:
                    onlineService.removeSession(...)
                4) Redis cleanup
                   lastSeen update (БД)
        */

        /**
         * heartbeat нужен для определения онлайн статуса и последнего входа в систему
         * */
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] {30_000, 30_000} )
                .setTaskScheduler(wsTaskScheduler());
        registry.setApplicationDestinationPrefixes("/app");

        // IMPORTANT для convertAndSendToUser
        registry.setUserDestinationPrefix("/user");

        /*
        После:
            STOMP CONNECT
            Spring создаёт:
            WebSocketSession
            STOMP sessions
            SimpUser (+ JWT)
            subscriptions
            и хранит это всё в памяти JVM
        **/


    }

    /**
     * SpringBroker поток
     * */
    @Bean
    public TaskScheduler wsTaskScheduler() {
        // SPRING THREAD POOL
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();

        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("https://evelentrp.online/");

        /*
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
         */
    }

    /**
     * Что-то с скриализацией WsResponse
     * */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> converters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setContentTypeResolver(message -> MimeTypeUtils.APPLICATION_JSON);
        converters.add(converter);

        return false;
    }


    /** Из сокет подключения с клиента через полученный JWT получает аккакнт и передает в сокет систему Spring
     С помощбью этого JWT работает в Spring (STOMP идея) **/
    /** Здесь Spring Security для WS */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}

/*
/topic → паблик (broadcast)
/queue → личные сообщения (point-to-point)
/topic
Один отправил → получили все подписанные

SEND /app/chat.send
→ /topic/messages
→ ВСЕ клиенты получают
Один отправил → получил конкретный пользователь

Spring делает магию:
convertAndSendToUser("vasya", "/queue/messages", msg)
реально уходит в:
/user/vasya/queue/messages

| Тип        | Куда шлём             | Кто получает    |
| ---------- | --------------------- | --------------- |
| Глобальный | `/topic/...`          | все             |
| Личный     | `/user/.../queue/...` | конкретный юзер |
* */