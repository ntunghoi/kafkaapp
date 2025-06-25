package com.ntunghoi.kafkaapp.repositories;

import com.ntunghoi.kafkaapp.entities.UserProfileEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfilesRepository extends CrudRepository<UserProfileEntity, Integer> {
    Optional<UserProfileEntity> findByEmail(String email);
}
