package com.piedrazul.api.iam.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.mapper.UserEntityMapper;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.iam.infrastructure.entity.UserJpaEntity;

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
}