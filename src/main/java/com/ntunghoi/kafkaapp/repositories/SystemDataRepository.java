package com.ntunghoi.kafkaapp.repositories;

import com.ntunghoi.kafkaapp.entities.RoleEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemDataRepository {
    Optional<RoleEntity> findByCode(String code);
}
