-- V4 (Sprint 7): avisos a los usuarios cuando se resuelven sus apuestas (HU-37).

create sequence notificacion_seq start with 1 increment by 50;

create table notificacion (
    id         bigint       not null,
    usuario_id bigint       not null,
    texto      varchar(500) not null,
    tipo       varchar(20)  not null,
    fecha      timestamp(6) not null,
    leida      boolean      not null,
    constraint pk_notificacion primary key (id),
    constraint fk_notificacion_usuario foreign key (usuario_id) references usuario
);

create index ix_notificacion_usuario on notificacion (usuario_id, leida);
