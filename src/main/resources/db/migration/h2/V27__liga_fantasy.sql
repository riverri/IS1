-- V27: liga fantasy (HU-81 a HU-83): cada usuario ficha siete futbolistas y suma puntos con sus partidos.
-- Si el creador borra a un jugador (o desaparece de la plantilla de la API), se borran también sus fichajes.

create sequence equipo_fantasy_seq start with 1 increment by 50;
create sequence fichaje_fantasy_seq start with 1 increment by 50;

create table equipo_fantasy (
    id         bigint       not null,
    version    bigint       not null,
    usuario_id bigint       not null,
    nombre     varchar(40)  not null,
    creado     timestamp(6) not null,
    constraint pk_equipo_fantasy primary key (id),
    constraint uk_equipo_fantasy_usuario unique (usuario_id),
    constraint fk_equipo_fantasy_usuario foreign key (usuario_id) references usuario
);

create table fichaje_fantasy (
    id         bigint       not null,
    equipo_id  bigint       not null,
    jugador_id bigint       not null,
    precio     numeric(5,1) not null,
    desde      timestamp(6) not null,
    hasta      timestamp(6),
    constraint pk_fichaje_fantasy primary key (id),
    constraint fk_fichaje_fantasy_equipo foreign key (equipo_id) references equipo_fantasy,
    constraint fk_fichaje_fantasy_jugador foreign key (jugador_id) references jugador on delete cascade
);

create index ix_fichaje_fantasy_equipo on fichaje_fantasy (equipo_id);
