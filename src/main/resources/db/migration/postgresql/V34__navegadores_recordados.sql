-- Persistent login tokens are isolated by browser series and can be revoked individually.
create table persistent_logins (
    username   varchar(255) not null,
    series     varchar(64)  not null,
    token      varchar(64)  not null,
    last_used  timestamp    not null,
    created_at timestamp    not null,
    user_agent varchar(255) not null,
    constraint pk_persistent_logins primary key (series)
);

create index ix_persistent_logins_username on persistent_logins (username);
