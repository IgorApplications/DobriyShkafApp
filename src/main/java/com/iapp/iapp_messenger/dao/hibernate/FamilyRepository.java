package com.iapp.iapp_messenger.dao.hibernate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    Optional<Family> findByFamilyNumber(String familyNumber);

}