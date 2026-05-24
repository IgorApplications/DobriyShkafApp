package com.iapp.iapp_messenger.controllers.ws_contollers;

import com.iapp.iapp_messenger.dao.dto.MessageRequest;
import com.iapp.iapp_messenger.dao.dto.WsResponse;
import com.iapp.iapp_messenger.dao.hibernate.PersonalMessage;
import com.iapp.iapp_messenger.dao.hibernate.PersonalMessageRepository;
import com.iapp.iapp_messenger.dao.hibernate.User;
import com.iapp.iapp_messenger.dao.hibernate.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Objects;

/**
 * WS Controller для личного чата.
 *
 * Если у пользователя есть защищенное WS соединение, то
 * ему будут приходить только НОВЫЕ сообщения в режиме реального времени:
 * client.subscribe("/user/queue/messages", (msg) => {});
 * В свою очередь ВЫСОКОУРОВНЕВЫЕ ошибки:
 * client.subscribe("/user/queue/errors", (msg) => {});
 *
 * Если пользователю нужна история сообщений из какого-то из СВОИХ ЛС чатов,
 * то нужно воспользоваться REST API (@see RESTPersonalMessageController)
 * */
@Controller
public class WSPersonalMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final PersonalMessageRepository messageRepository;

    public WSPersonalMessageController(SimpMessagingTemplate messagingTemplate,
                                       UserRepository userRepository,
                                       PersonalMessageRepository messageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }


    /**
     * Отправка сообщений после успешного защищенного подключения WS:
     * client.publish({
     * destination: "/app/chat.private",
     * headers: {
     * "content-type": "application/json"
     * },
     * body: JSON.stringify({
     * recipientLogin: "iapp",
     * text: "hello from WS"
     * })
     * });
     * */
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(MessageRequest messageRequest, Principal principal) {
        User sender = (User) ((Authentication) principal).getPrincipal();

        User recipient = userRepository
                .findByLogin(messageRequest.getRecipientLogin())
                .orElse(null);

        if (recipient == null) {
            sendErrorToUser("Recipient user login not found", principal);
            return;
        }

        if (Objects.equals(recipient.getId(), sender.getId())) {
            sendErrorToUser("It is forbidden to send messages to yourself", principal);
            return;
        }

        PersonalMessage message = new PersonalMessage(
                sender.getId(),
                recipient.getId(),
                messageRequest.getText()
        );

        saveMessageSafety(message);

        messagingTemplate.convertAndSendToUser(
                recipient.getLogin(),
                "/queue/messages",
                WsResponse.ok(message)
        );

        messagingTemplate.convertAndSendToUser(
                sender.getLogin(),
                "/queue/messages",
                WsResponse.ok(message)
        );
    }

    /**
     * Высокоуровневый канал для ошибок.
     * Этот канал для ошибок используется после успешного защищенного подключения с помощью JWT:
     * client.subscribe("/user/queue/errors", (msg) => {});
     * До этого:
     * client.onStompError = (frame) => {};
     * */
    private void sendErrorToUser(String error, Principal principal) {
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                WsResponse.error(error)
        );
    }

    /**
     * Гарантированно сохранеяет сообщение в базу данных.
     * Даже если после успешного вызова save где-то упало исклюбчение.
     * */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PersonalMessage saveMessageSafety(PersonalMessage message) {
        return messageRepository.save(message);
    }


}

/**
 Передавать JWT при подключении

 const socket = new SockJS("/ws");
 const stompClient = Stomp.over(socket);

 stompClient.connect({
 Authorization: "Bearer " + token
 }, onConnected);
 */

/**
 | HTTP           | WebSocket + STOMP         |
 | -------------- | ------------------------- |
 | POST /api/send | SEND /app/chat.send       |
 | GET /messages  | SUBSCRIBE /topic/messages |


 @Controller
 public class ChatController {

 // TODO STOMP!??
 @MessageMapping("/chat.send") // КЛИЕНТ отправляет сюда: /app/chat.send
 @SendTo("/topic/messages") // /topic и /queue это один из выходов от сервера к клиенту
 // Клиент → /app/... → Controller → /topic/... → Клиенты
 public Message send(Message message) {
 return message;
 }

 // ДЛЯ ЛС:
 // /user/{username}/queue/messages
 }
 */

/**
 WS → realtime
 REST → данные
 Разделение ответственности
 Что	Где
 отправка сообщения	WS
 получение истории	REST
 проверка existsLogin	REST

 WS не для bulk-данных
 REST проще кешировать
 REST проще тестировать
 WS должен быть lightweight
 **/

/**
Если пользователь ONLINE
messagingTemplate.convertAndSendToUser(
    "vasya",
    "/queue/messages",
    message
);

Spring найдёт его WebSocket-сессию и отправит

Если OFFLINE
НИКУДА не отправится
Сообщение потеряется

ВАЖНАЯ ФИЧА (ТODO):
Как делают нормальные системы
Сохраняют сообщение в БД
Пытаются отправить через WS
Если пользователь оффлайн:
он получит позже через HTTP (история)

ОБЩАЯ СХЕМА:
1. SEND → сервер
2. сервер → сохраняет в БД
3. сервер → пытается отправить через WS
4. если получатель онлайн → получает сразу
5. если нет → прочитает потом через API
*/

/**
 * /queue/messages -> success
 * /queue/errors -> error
 * /queue ---> personal callback (WS)
 * */
/**
 * CALLBACK: /queue/errors
 * */
/** Персонализированный канал для ошибок /queue (login)*/

/**
 * Клиент отправляет сюда: /app/chat.private
 * Ответ от сервера через персонализированный /queue: ДЛЯ ЛС:
 * !!!? /user/{username}/queue/messages
 * */
