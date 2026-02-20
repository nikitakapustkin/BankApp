package org.nikitakapustkin.application.ports.in.queries;

import java.util.List;
import org.nikitakapustkin.domain.models.User;

public interface GetUsersQuery {
  List<User> getUsers(String hairColorStr, String sexStr);
}
