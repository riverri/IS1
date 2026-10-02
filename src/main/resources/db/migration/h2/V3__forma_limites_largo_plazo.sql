-- V3 (Sprint 6): forma reciente de los equipos, límites de apuesta y apuestas a largo plazo.

-- HU-02: factor de forma reciente; los equipos existentes empiezan en forma normal
alter table equipo add column forma enum ('BUENA','MALA','MUY_BUENA','MUY_MALA','NORMAL') default 'NORMAL' not null;

-- HU-07: límites de apuesta que fija el creador (una sola fila)
create table limites (
    id              bigint        not null,
    importe_minimo  numeric(12,2) not null,
    importe_maximo  numeric(12,2) not null,
    max_selecciones integer       not null,
    constraint pk_limites primary key (id)
);
insert into limites (id, importe_minimo, importe_maximo, max_selecciones) values (1, 1.00, 500.00, 10);

-- HU-44 y HU-45: mercados a largo plazo con sus candidatos
create sequence mercado_seq start with 1 increment by 50;
create sequence candidato_seq start with 1 increment by 50;

create table mercado (
    id         bigint       not null,
    nombre     varchar(255) not null,
    deporte    enum ('AUTOMOVILISMO','BALONCESTO','FUTBOL','MOTOCICLISMO','TENIS') not null,
    cierre     timestamp(6) not null,
    estado     enum ('ABIERTO','ANULADO','CERRADO','RESUELTO') not null,
    ganador_id bigint,
    constraint pk_mercado primary key (id)
);

create table candidato (
    id         bigint        not null,
    mercado_id bigint        not null,
    nombre     varchar(255)  not null,
    cuota      numeric(8,2)  not null,
    equipo_id  bigint,
    constraint pk_candidato primary key (id),
    constraint fk_candidato_mercado foreign key (mercado_id) references mercado,
    constraint fk_candidato_equipo foreign key (equipo_id) references equipo
);

alter table mercado add constraint fk_mercado_ganador foreign key (ganador_id) references candidato;

-- Una selección es el resultado de un evento o el candidato de un mercado
alter table seleccion alter column evento_id set null;
alter table seleccion alter column pronostico set null;
alter table seleccion add column candidato_id bigint;
alter table seleccion add constraint fk_seleccion_candidato foreign key (candidato_id) references candidato;
