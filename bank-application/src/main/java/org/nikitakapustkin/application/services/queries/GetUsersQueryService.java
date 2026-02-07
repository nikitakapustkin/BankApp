package org.nikitakapustkin.application.services.queries;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.queries.GetUsersQuery;
import org.nikitakapustkin.application.ports.out.LoadUsersPort;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;
import org.nikitakapustkin.domain.models.User;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class GetUsersQueryService implements GetUsersQuery {

    private final LoadUsersPort loadUsersPort;

    @Override
    public List<User> getUsers(String hairColorStr, String sexStr) {
        HairColor hairColor = hairColorStr != null
                ? HairColor.valueOf(hairColorStr.toUpperCase(Locale.ROOT))
                : null;
        Sex sex = sexStr != null
                ? Sex.valueOf(sexStr.toUpperCase(Locale.ROOT))
                : null;

        return loadUsersPort.findAllByFilters(hairColor, sex);
    }
}
