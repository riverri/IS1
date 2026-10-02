-- V8 (Sprint 11): ligas privadas (HU-51), marcador de los partidos y otros tipos de apuesta (HU-52).

create sequence liga_seq start with 1 increment by 50;

create table liga (
    id         bigint       not null,
    nombre     varchar(40)  not null,
    codigo     varchar(6)   not null,
    creador_id bigint       not null,
    creada     timestamp(6) not null,
    constraint pk_liga primary key (id),
    constraint uk_liga_codigo unique (codigo),
    constraint fk_liga_creador foreign key (creador_id) references usuario
);

create table liga_miembro (
    liga_id    bigint not null,
    usuario_id bigint not null,
    constraint pk_liga_miembro primary key (liga_id, usuario_id),
    constraint fk_liga_miembro_liga foreign key (liga_id) references liga,
    constraint fk_liga_miembro_usuario foreign key (usuario_id) references usuario
);

alter table evento add column goles_local integer;
alter table evento add column goles_visitante integer;

alter table seleccion add column especial
    enum ('AMBOS_NO','AMBOS_SI','DOBLE_12','DOBLE_1X','DOBLE_X2','MAS_2_5','MENOS_2_5');
