package com.iapp.iapp_messenger.dao.hibernate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);

    /**
     * Возвращает логины, которые существуют
     * */
    @Query("""
        SELECT u.login
        FROM User u
        WHERE u.login IN :logins
    """)
    List<String> findExistingLogins(@Param("logins") List<String> logins);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.login IN :logins
    """)
    List<User> findAllByLogins(@Param("logins") List<String> logins);
}

// магические методы DAO JPA, генерирует её сам по имени метода