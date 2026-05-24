package com.iapp.iapp_messenger.dao.hibernate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PersonalMessageRepository extends JpaRepository<PersonalMessage, Long> {

    @Query("""
    SELECT m FROM PersonalMessage m
    WHERE (m.senderId = :a AND m.recipientId = :b)
       OR (m.senderId = :b AND m.recipientId = :a)
    ORDER BY m.time ASC""")
    List<PersonalMessage> findDialog(Long a, Long b, Pageable pageable);
}
