package com.iapp.iapp_messenger.config;

import com.iapp.iapp_messenger.dao.dto.WsResponse;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;

/**
 * Глобальный try-catch для WsControllers
 * */
@ControllerAdvice
public class WsExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public WsExceptionHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageExceptionHandler
    public void handle(Exception ex, Principal principal) {
        WsResponse<String> wsResponse;
        if (ex instanceof IllegalArgumentException) {
            wsResponse = WsResponse.error(ex.getMessage());
        } else {
            wsResponse = WsResponse.error("Internal server error");
        }
        if (principal == null) return;

        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                wsResponse
        );
    }
}