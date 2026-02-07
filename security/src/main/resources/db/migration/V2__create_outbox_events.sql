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
