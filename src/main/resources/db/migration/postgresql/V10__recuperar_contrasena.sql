-- V10: enlaces para recuperar la contraseña (HU-17). Se guarda el resumen SHA-256 del código, no el código.

create sequence token_recuperacion_seq start with 1 increment by 50;

create table token_recuperacion (
    id         bigint      not null,
    usuario_id bigint      not null,
    hash       varchar(64) not null,
    caduca     timestamp   not null,
    usado      boolean     not null,
    constraint pk_token_recuperacion primary key (id),
    constraint uk_token_recuperacion_hash unique (hash),
    constraint fk_token_recuperacion_usuario foreign key (usuario_id) references usuario
);

create index ix_token_recuperacion_usuario on token_recuperacion (usuario_id);
