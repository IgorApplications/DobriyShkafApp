package com.iapp.iapp_messenger.dao.hibernate;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "personal_messages")
public class PersonalMessage {

    // генерирует id автоматически
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;
    private Long recipientId;
    private Long time;
    private String text;

    public PersonalMessage() {}

    public PersonalMessage(long senderId, long recipientId, String text) {
        this.senderId = senderId;
        time = new Date().getTime();
        this.recipientId = recipientId;
        this.text = text;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(long recipientId) {
        this.recipientId = recipientId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
