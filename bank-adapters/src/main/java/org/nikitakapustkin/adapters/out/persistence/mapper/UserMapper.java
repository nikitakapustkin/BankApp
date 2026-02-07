package org.nikitakapustkin.adapters.out.persistence.mapper;

import org.nikitakapustkin.domain.models.User;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.UserEntity;

public final class UserMapper {
    private UserMapper() {}

    public static User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .login(e.getLogin())
                .name(e.getName())
                .age(e.getAge())
                .sex(e.getSex())
                .hairColor(e.getHairColor())
                .build();
    }

    public static UserEntity toJpaEntity(User d) {
        UserEntity e = new UserEntity();
        updateJpaEntity(d, e);
        return e;
    }

    public static void updateJpaEntity(User d, UserEntity e) {
        if (d.getId() != null) {
            e.setId(d.getId());
        }
        e.setLogin(d.getLogin());
        e.setName(d.getName());
        e.setAge(d.getAge());
        e.setSex(d.getSex());
        e.setHairColor(d.getHairColor());
    }
}
