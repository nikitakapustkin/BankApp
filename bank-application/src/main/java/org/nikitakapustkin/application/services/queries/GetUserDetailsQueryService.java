package org.nikitakapustkin.application.services.queries;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.queries.GetUserDetailsQuery;
import org.nikitakapustkin.application.ports.out.LoadAccountsPort;
import org.nikitakapustkin.application.ports.out.LoadFriendsPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;
import org.nikitakapustkin.domain.models.Account;
import org.nikitakapustkin.domain.models.User;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetUserDetailsQueryService implements GetUserDetailsQuery {

    private final LoadUserPort loadUserPort;
    private final LoadFriendsPort loadFriendsPort;
    private final LoadAccountsPort loadAccountsPort;

    @Override
    public UserDetails getUserDetails(UUID userId) {
        User user = loadUserPort.loadUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        List<UUID> friendIds = loadFriendsPort.loadFriendsIds(user.getId());
        List<String> friendsLogins = friendIds.stream()
                .map(loadUserPort::loadUserById)
                .map(opt -> opt.orElseThrow(() -> new UserNotFoundException("Friend not found")))
                .map(User::getLogin)
                .toList();

        List<Account> accounts = loadAccountsPort.loadAccounts(user.getId());

        return new UserDetails(user, friendsLogins, accounts);
    }
}
