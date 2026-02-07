create table user_event (
    id uuid primary key,
    event_id uuid unique,
    correlation_id uuid,
    user_id uuid,
    event_type varchar(255),
    event_time timestamp with time zone,
    event_description varchar(255),
    payload_type varchar(255),
    payload text
);

create table account_event (
    id uuid primary key,
    event_id uuid unique,
    correlation_id uuid,
    account_id uuid,
    event_type varchar(255),
    event_time timestamp with time zone,
    event_description varchar(255),
    payload_type varchar(255),
    payload text
);

create table transaction_event (
    id uuid primary key,
    event_id uuid unique,
    transaction_id uuid unique,
    correlation_id uuid,
    account_id uuid,
    transaction_type varchar(32),
    amount numeric(19, 2),
    created_at timestamp with time zone,
    event_type varchar(255),
    event_time timestamp with time zone,
    event_description varchar(255),
    payload_type varchar(255),
    payload text
);
