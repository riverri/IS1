-- Existing accounts remain verified; new web registrations must confirm mailbox ownership.
alter table usuario add column email_verificado boolean default true not null;

create sequence token_verificacion_correo_seq start with 1 increment by 50;

create table token_verificacion_correo (
    id         bigint      not null,
    usuario_id bigint      not null,
    hash       varchar(64) not null,
    caduca     timestamp   not null,
    usado      boolean     not null,
    constraint pk_token_verificacion_correo primary key (id),
    constraint uk_token_verificacion_correo_hash unique (hash),
    constraint fk_token_verificacion_correo_usuario foreign key (usuario_id) references usuario
);

create index ix_token_verificacion_correo_usuario on token_verificacion_correo (usuario_id);
