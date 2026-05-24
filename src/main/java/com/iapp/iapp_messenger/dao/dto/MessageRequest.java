package com.iapp.iapp_messenger.dao.dto;

public class MessageRequest {

    private String recipientLogin;
    private String text;

    public MessageRequest() {}


    public String getRecipientLogin() {
        return recipientLogin;
    }

    public void setRecipientLogin(String recipientLogin) {
        this.recipientLogin = recipientLogin;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
