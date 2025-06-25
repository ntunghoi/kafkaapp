package com.ntunghoi.kafkaapp.repositories;

import com.ntunghoi.kafkaapp.entities.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository
        extends CrudRepository<RoleEntity, String> {
}
