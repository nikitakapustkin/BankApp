package org.nikitakapustkin.domain.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum EventType {
    @JsonEnumDefaultValue
    UNKNOWN,

    USER_CREATED,
    FRIEND_ADDED,
    FRIEND_REMOVED,

    ACCOUNT_CREATED,
    ACCOUNT_DEPOSIT,
    ACCOUNT_WITHDRAWAL,
    ACCOUNT_TRANSFER,

    TRANSACTION_CREATED
}
