package com.ntunghoi.kafkaapp.services;

import com.ntunghoi.kafkaapp.entities.RoleEntity;
import com.ntunghoi.kafkaapp.repositories.RolesRepository;
import com.ntunghoi.kafkaapp.repositories.SystemDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SystemDataService {
    private final RolesRepository rolesRepository;

    @Autowired
    public SystemDataService(
            RolesRepository rolesRepository
    ) {
        this.rolesRepository = rolesRepository;
    }

    public static class SystemDataRepositoryImpl
            implements SystemDataRepository {
        private final Map<String, RoleEntity> roleByCode;

        public SystemDataRepositoryImpl(Map<String, RoleEntity> roleByCode) {
            this.roleByCode = roleByCode;
        }

        @Override
        public Optional<RoleEntity> findByCode(String code) {
            return Optional.of(roleByCode.get(code));
        }
    }

    @Bean
    public SystemDataRepository systemDateRepository() {
        Map<String, RoleEntity> roleByCode = StreamSupport
                .stream(
                        rolesRepository.findAll().spliterator(), false
                ).collect(Collectors.toMap(RoleEntity::getCode, role -> role));

        return new SystemDataRepositoryImpl(roleByCode);
    }
}
