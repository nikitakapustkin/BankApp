create table if not exists security_users (
                                             user_id   uuid primary key,
                                             login    varchar(255) not null unique,
    password varchar(255) not null,
    role     varchar(20)  not null
    );

INSERT INTO security_users (user_id, login, password, role)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'admin',
           '$2b$08$jePoAkpALZJ0TC0LTngmEO68mviXTFH0KrF9bPRfxl0kSjQYVZCZi',
           'ADMIN'
       )
    ON CONFLICT (login) DO NOTHING;