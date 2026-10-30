package com.piedrazul.api.iam.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.entity.UserJpaEntity;
import com.piedrazul.api.iam.mapper.UserEntityMapper;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.iam.domain.RoleEnum;

@Repository
public class SqliteUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserEntityMapper userEntityMapper;

    public SqliteUserRepository(SpringDataUserRepository springDataUserRepository,
            UserEntityMapper userEntityMapper) {
        this.springDataUserRepository = springDataUserRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = userEntityMapper.toEntity(user);
        UserJpaEntity savedEntity = springDataUserRepository.save(entity);
        return userEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(String id) {
        return springDataUserRepository.findById(id)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public List<User> findAllById(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return springDataUserRepository.findAllById(ids)
                .stream()
                .map(userEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findByNumeroIdentificacion(String numeroIdentificacion) {
        return springDataUserRepository.findByNumeroIdentificacion(numeroIdentificacion)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findFirstByEmail(String email) {
        return springDataUserRepository.findFirstByEmail(email)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public List<User> findTop10ByRoleAndNumeroIdentificacionStartingWith(RoleEnum role, String prefijo) {
        return springDataUserRepository
                .findTop10ByRoleAndNumeroIdentificacionStartingWithOrderByNumeroIdentificacionAsc(role, prefijo)
                .stream()
                .map(userEntityMapper::toDomain)
                .toList();
    }
}
