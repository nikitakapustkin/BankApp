package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;
import org.nikitakapustkin.domain.models.User;

import java.util.List;

public interface LoadUsersPort {

    List<User> findAllByFilters(HairColor hairColor, Sex sex);
}
