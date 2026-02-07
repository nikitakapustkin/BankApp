// application.ports.in.queries.GetUserDetailsQuery.java
package org.nikitakapustkin.application.ports.in.queries;

import org.nikitakapustkin.domain.models.Account;
import org.nikitakapustkin.domain.models.User;

import java.util.List;
import java.util.UUID;

public interface GetUserDetailsQuery {
    UserDetails getUserDetails(UUID userId);

    record UserDetails(User user, List<String> friendsLogins, List<Account> accounts) {}
}
