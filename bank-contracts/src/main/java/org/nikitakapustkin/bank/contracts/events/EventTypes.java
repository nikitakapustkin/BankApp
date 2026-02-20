package org.nikitakapustkin.bank.contracts.events;

public final class EventTypes {
  public static final String UNKNOWN = "unknown";
  public static final String USER_CREATED = "user.created";
  public static final String FRIEND_ADDED = "friend.added";
  public static final String FRIEND_REMOVED = "friend.removed";
  public static final String ACCOUNT_CREATED = "account.created";
  public static final String ACCOUNT_DEPOSIT = "account.deposit";
  public static final String ACCOUNT_WITHDRAWAL = "account.withdrawal";
  public static final String ACCOUNT_TRANSFER = "account.transfer";
  public static final String TRANSACTION_CREATED = "transaction.created";

  private EventTypes() {}
}
