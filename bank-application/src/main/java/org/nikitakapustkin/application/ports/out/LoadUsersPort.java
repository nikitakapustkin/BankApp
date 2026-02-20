package org.nikitakapustkin.application.ports.out;

import java.util.List;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;
import org.nikitakapustkin.domain.models.User;

public interface LoadUsersPort {

  List<User> findAllByFilters(HairColor hairColor, Sex sex);
}
