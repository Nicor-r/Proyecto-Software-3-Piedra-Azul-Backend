package com.piedrazul.api.iam.mapper;

import org.springframework.stereotype.Component;

import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.UserJpaEntity;

@Component
public class UserEntityMapper {

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole()
        );
    }
}