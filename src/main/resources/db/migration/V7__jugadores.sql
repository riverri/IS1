-- V7: plantillas de los equipos (jugadores con su posición, dorsal y nota).

create sequence jugador_seq start with 1 increment by 50;

create table jugador (
    id               bigint       not null,
    equipo_id        bigint       not null,
    nombre           varchar(255) not null,
    posicion         enum ('CENTROCAMPISTA','DEFENSA','DELANTERO','PORTERO') not null,
    dorsal           integer,
    nacionalidad     varchar(255),
    fecha_nacimiento date,
    nota             float(53)    not null,
    id_externo       integer,
    constraint pk_jugador primary key (id),
    constraint uk_jugador_id_externo unique (id_externo),
    constraint fk_jugador_equipo foreign key (equipo_id) references equipo
);

create index ix_jugador_equipo on jugador (equipo_id);
