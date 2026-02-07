package org.nikitakapustkin.security.constants;

public final class BankApiPaths {
    public static final String USERS = "/users";
    public static final String USER_BY_ID = "/users/{userId}";
    public static final String USERS_FRIENDS = "/users/friends";
    public static final String USER_ACCOUNTS = "/users/{userId}/accounts";
    public static final String ACCOUNTS = "/accounts";
    public static final String ACCOUNT_BY_ID = "/accounts/{accountId}";
    public static final String ACCOUNT_DEPOSIT = "/accounts/{accountId}/deposit";
    public static final String ACCOUNT_WITHDRAW = "/accounts/{accountId}/withdraw";
    public static final String ACCOUNT_TRANSFER = "/accounts/{fromAccountId}/transfer";
    public static final String TRANSACTIONS = "/transactions";

    private BankApiPaths() {
    }
}
