create sequence registro_pendiente_seq start with 1 increment by 50;

create table registro_pendiente (
    id         bigint       not null,
    email      varchar(255) not null,
    token_hash varchar(64)  not null,
    caduca     timestamp    not null,
    constraint pk_registro_pendiente primary key (id),
    constraint uk_registro_pendiente_email unique (email),
    constraint uk_registro_pendiente_token_hash unique (token_hash)
);
