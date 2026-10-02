-- Esquema completo para PostgreSQL (base de datos del servidor, ver README).
-- Equivale a las migraciones V1 a V8 de H2 (carpeta h2/). A partir de aquí, cada cambio del esquema
-- necesita su script en las dos carpetas, con el mismo número (V9__..., V10__...).

create sequence usuario_seq start with 1 increment by 50;
create sequence competicion_seq start with 1 increment by 50;
create sequence equipo_seq start with 1 increment by 50;
create sequence evento_seq start with 1 increment by 50;
create sequence apuesta_seq start with 1 increment by 50;
create sequence seleccion_seq start with 1 increment by 50;
create sequence mercado_seq start with 1 increment by 50;
create sequence candidato_seq start with 1 increment by 50;
create sequence notificacion_seq start with 1 increment by 50;
create sequence jugador_seq start with 1 increment by 50;
create sequence liga_seq start with 1 increment by 50;

create table usuario (
    id             bigint        not null,
    email          varchar(255)  not null,
    nombre         varchar(255)  not null,
    password_hash  varchar(255)  not null,
    rol            varchar(255)  not null check (rol in ('USUARIO', 'CREADOR')),
    saldo          numeric(12,2) not null,
    ultima_recarga timestamp(6)  not null,
    limite_diario  numeric(12,2),
    limite_semanal numeric(12,2),
    pausa_hasta    timestamp(6),
    eliminado      boolean       not null default false,
    constraint pk_usuario primary key (id),
    constraint uk_usuario_email unique (email)
);

create table competicion (
    id      bigint       not null,
    nombre  varchar(255) not null,
    deporte varchar(255) not null check (deporte in ('FUTBOL', 'BALONCESTO', 'TENIS', 'AUTOMOVILISMO', 'MOTOCICLISMO')),
    constraint pk_competicion primary key (id)
);

create table equipo (
    id         bigint           not null,
    nombre     varchar(255)     not null,
    deporte    varchar(255)     not null check (deporte in ('FUTBOL', 'BALONCESTO', 'TENIS', 'AUTOMOVILISMO', 'MOTOCICLISMO')),
    calidad    double precision not null,
    forma      varchar(255)     not null default 'NORMAL'
               check (forma in ('MUY_MALA', 'MALA', 'NORMAL', 'BUENA', 'MUY_BUENA')),
    escudo_url varchar(500),
    id_externo integer,
    constraint pk_equipo primary key (id),
    constraint uk_equipo_id_externo unique (id_externo)
);

create table equipo_competiciones (
    equipo_id        bigint not null,
    competiciones_id bigint not null,
    constraint pk_equipo_competiciones primary key (competiciones_id, equipo_id),
    constraint fk_equipo_competiciones_equipo foreign key (equipo_id) references equipo,
    constraint fk_equipo_competiciones_competicion foreign key (competiciones_id) references competicion
);

create table evento (
    id              bigint       not null,
    competicion_id  bigint       not null,
    local_id        bigint       not null,
    visitante_id    bigint       not null,
    fecha_hora      timestamp(6) not null,
    estado          varchar(255) not null check (estado in ('PROGRAMADO', 'SUSPENDIDO', 'FINALIZADO', 'ANULADO')),
    resultado       varchar(255) check (resultado in ('LOCAL', 'EMPATE', 'VISITANTE')),
    goles_local     integer,
    goles_visitante integer,
    fase            varchar(255),
    id_externo      bigint,
    constraint pk_evento primary key (id),
    constraint uk_evento_id_externo unique (id_externo),
    constraint fk_evento_competicion foreign key (competicion_id) references competicion,
    constraint fk_evento_local foreign key (local_id) references equipo,
    constraint fk_evento_visitante foreign key (visitante_id) references equipo
);

create table apuesta (
    id         bigint        not null,
    usuario_id bigint        not null,
    importe    numeric(12,2) not null,
    cuota      numeric(12,2) not null,
    estado     varchar(255)  not null check (estado in ('ACTIVA', 'GANADA', 'PERDIDA', 'ANULADA', 'CANCELADA')),
    pagado     numeric(14,2) not null,
    fecha      timestamp(6)  not null,
    constraint pk_apuesta primary key (id),
    constraint fk_apuesta_usuario foreign key (usuario_id) references usuario
);

create table mercado (
    id         bigint       not null,
    nombre     varchar(255) not null,
    deporte    varchar(255) not null check (deporte in ('FUTBOL', 'BALONCESTO', 'TENIS', 'AUTOMOVILISMO', 'MOTOCICLISMO')),
    cierre     timestamp(6) not null,
    estado     varchar(255) not null check (estado in ('ABIERTO', 'CERRADO', 'RESUELTO', 'ANULADO')),
    ganador_id bigint,
    constraint pk_mercado primary key (id)
);

create table candidato (
    id         bigint       not null,
    mercado_id bigint       not null,
    nombre     varchar(255) not null,
    cuota      numeric(8,2) not null,
    equipo_id  bigint,
    constraint pk_candidato primary key (id),
    constraint fk_candidato_mercado foreign key (mercado_id) references mercado,
    constraint fk_candidato_equipo foreign key (equipo_id) references equipo
);

alter table mercado add constraint fk_mercado_ganador foreign key (ganador_id) references candidato;

create table seleccion (
    id           bigint       not null,
    apuesta_id   bigint       not null,
    evento_id    bigint,
    pronostico   varchar(255) check (pronostico in ('LOCAL', 'EMPATE', 'VISITANTE')),
    especial     varchar(255) check (especial in ('DOBLE_1X', 'DOBLE_X2', 'DOBLE_12', 'MAS_2_5', 'MENOS_2_5',
                                                  'AMBOS_SI', 'AMBOS_NO')),
    candidato_id bigint,
    cuota        numeric(8,2) not null,
    estado       varchar(255) not null check (estado in ('PENDIENTE', 'ACERTADA', 'FALLADA', 'ANULADA')),
    constraint pk_seleccion primary key (id),
    constraint fk_seleccion_apuesta foreign key (apuesta_id) references apuesta,
    constraint fk_seleccion_evento foreign key (evento_id) references evento,
    constraint fk_seleccion_candidato foreign key (candidato_id) references candidato
);

create index ix_seleccion_evento on seleccion (evento_id);

create table limites (
    id                bigint        not null,
    importe_minimo    numeric(12,2) not null,
    importe_maximo    numeric(12,2) not null,
    max_selecciones   integer       not null,
    juego_responsable boolean       not null default true,
    constraint pk_limites primary key (id)
);

insert into limites (id, importe_minimo, importe_maximo, max_selecciones) values (1, 1.00, 500.00, 10);

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

create table jugador (
    id               bigint           not null,
    equipo_id        bigint           not null,
    nombre           varchar(255)     not null,
    posicion         varchar(255)     not null check (posicion in ('PORTERO', 'DEFENSA', 'CENTROCAMPISTA', 'DELANTERO')),
    dorsal           integer,
    nacionalidad     varchar(255),
    fecha_nacimiento date,
    nota             double precision not null,
    id_externo       integer,
    constraint pk_jugador primary key (id),
    constraint uk_jugador_id_externo unique (id_externo),
    constraint fk_jugador_equipo foreign key (equipo_id) references equipo
);

create index ix_jugador_equipo on jugador (equipo_id);

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
