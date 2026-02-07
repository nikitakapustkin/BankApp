package org.nikitakapustkin.security.constants;

public final class SecurityApiPaths {
    public static final String ROOT = "/";
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";

    public static final String USERS = "/users";
    public static final String USERS_ALL = "/users/**";
    public static final String USERS_REGISTER = "/users/register";
    public static final String USERS_ME = "/users/me";
    public static final String USERS_ME_ALL = "/users/me/**";
    public static final String USERS_ID = "/users/{userId}";
    public static final String USERS_ME_FRIENDS = "/users/me/friends";

    public static final String ACCOUNTS = "/accounts";
    public static final String ACCOUNTS_ALL = "/accounts/**";
    public static final String ACCOUNTS_BY_USER = "/accounts/users/{userId}";
    public static final String ACCOUNT_BY_ID = "/accounts/{accountId}";
    public static final String USERS_ME_ACCOUNTS = "/users/me/accounts";
    public static final String USERS_ME_ACCOUNT_BY_ID = "/users/me/accounts/{accountId}";
    public static final String USERS_ME_ACCOUNT_DEPOSIT = "/users/me/accounts/{accountId}/deposit";
    public static final String USERS_ME_ACCOUNT_WITHDRAW = "/users/me/accounts/{accountId}/withdraw";
    public static final String USERS_ME_ACCOUNT_TRANSFER = "/users/me/accounts/{fromAccountId}/transfer";
    public static final String USERS_ME_TRANSACTIONS = "/users/me/transactions";
    public static final String TRANSACTIONS = "/transactions";
    public static final String TRANSACTIONS_ALL = "/transactions/**";
    public static final String EVENTS = "/events";
    public static final String EVENTS_ALL = "/events/**";

    private SecurityApiPaths() {
    }
}
