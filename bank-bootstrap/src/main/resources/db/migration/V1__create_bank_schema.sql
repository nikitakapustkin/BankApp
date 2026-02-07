create table users (
    id uuid primary key,
    login varchar(255) not null unique,
    name varchar(255) not null,
    sex varchar(32) not null,
    hair_color varchar(32) not null,
    age integer not null
);

create table accounts (
    account_id uuid primary key,
    user_id uuid not null,
    owner_login varchar(255) not null,
    balance numeric(19, 2) not null,
    version bigint not null default 0
);

alter table accounts
    add constraint fk_accounts_user
        foreign key (user_id) references users(id) on delete cascade;

create table transactions (
    id uuid primary key,
    transaction_type varchar(32) not null,
    account_id uuid not null,
    amount numeric(19, 2) not null,
    created_at timestamp with time zone not null
);

alter table transactions
    add constraint fk_transactions_account
        foreign key (account_id) references accounts(account_id) on delete cascade;

create table user_friends (
    user_id uuid not null,
    friend_id uuid not null,
    primary key (user_id, friend_id),
    constraint fk_user_friends_user foreign key (user_id) references users(id) on delete cascade,
    constraint fk_user_friends_friend foreign key (friend_id) references users(id) on delete cascade
);

create table outbox_events (
    id uuid primary key,
    topic varchar(255) not null,
    event_key varchar(255),
    event_type varchar(255) not null,
    payload text not null,
    status varchar(32) not null,
    attempts integer not null,
    created_at timestamp with time zone not null,
    last_attempt_at timestamp with time zone,
    published_at timestamp with time zone,
    last_error text
);

create index idx_outbox_events_status_created_at
    on outbox_events (status, created_at);
