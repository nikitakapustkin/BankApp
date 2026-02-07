package org.nikitakapustkin.application.ports.in.queries;

import org.nikitakapustkin.domain.models.User;

import java.util.List;

public interface GetUsersQuery {
    List<User> getUsers(String hairColorStr, String sexStr);
}
