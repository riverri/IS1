-- V1: esquema de la base de datos tras el Sprint 4 (apuestas simples y combinadas).
-- Las bases de datos creadas antes de usar Flyway con este esquema se toman como versión 1
-- (spring.flyway.baseline-on-migrate) y empiezan a aplicar migraciones desde la V2.

create sequence apuesta_seq start with 1 increment by 50;
create sequence competicion_seq start with 1 increment by 50;
create sequence equipo_seq start with 1 increment by 50;
create sequence evento_seq start with 1 increment by 50;
create sequence seleccion_seq start with 1 increment by 50;
create sequence usuario_seq start with 1 increment by 50;

create table usuario (
    id             bigint         not null,
    email          varchar(255)   not null,
    nombre         varchar(255)   not null,
    password_hash  varchar(255)   not null,
    rol            enum ('CREADOR','USUARIO') not null,
    saldo          numeric(12,2)  not null,
    ultima_recarga timestamp(6)   not null,
    constraint pk_usuario primary key (id),
    constraint uk_usuario_email unique (email)
);

create table competicion (
    id      bigint       not null,
    nombre  varchar(255) not null,
    deporte enum ('AUTOMOVILISMO','BALONCESTO','FUTBOL','MOTOCICLISMO','TENIS') not null,
    constraint pk_competicion primary key (id)
);

create table equipo (
    id         bigint       not null,
    nombre     varchar(255) not null,
    deporte    enum ('AUTOMOVILISMO','BALONCESTO','FUTBOL','MOTOCICLISMO','TENIS') not null,
    calidad    float(53)    not null,
    escudo_url varchar(500),
    constraint pk_equipo primary key (id)
);

create table equipo_competiciones (
    equipo_id        bigint not null,
    competiciones_id bigint not null,
    constraint pk_equipo_competiciones primary key (competiciones_id, equipo_id),
    constraint fk_equipo_competiciones_equipo foreign key (equipo_id) references equipo,
    constraint fk_equipo_competiciones_competicion foreign key (competiciones_id) references competicion
);

create table evento (
    id             bigint       not null,
    competicion_id bigint       not null,
    local_id       bigint       not null,
    visitante_id   bigint       not null,
    fecha_hora     timestamp(6) not null,
    estado         enum ('ANULADO','FINALIZADO','PROGRAMADO','SUSPENDIDO') not null,
    resultado      enum ('EMPATE','LOCAL','VISITANTE'),
    fase           varchar(255),
    constraint pk_evento primary key (id),
    constraint fk_evento_competicion foreign key (competicion_id) references competicion,
    constraint fk_evento_local foreign key (local_id) references equipo,
    constraint fk_evento_visitante foreign key (visitante_id) references equipo
);

create table apuesta (
    id         bigint        not null,
    usuario_id bigint        not null,
    importe    numeric(12,2) not null,
    cuota      numeric(12,2) not null,
    estado     enum ('ACTIVA','ANULADA','CANCELADA','GANADA','PERDIDA') not null,
    pagado     numeric(14,2) not null,
    fecha      timestamp(6)  not null,
    constraint pk_apuesta primary key (id),
    constraint fk_apuesta_usuario foreign key (usuario_id) references usuario
);

create table seleccion (
    id         bigint       not null,
    apuesta_id bigint       not null,
    evento_id  bigint       not null,
    pronostico enum ('EMPATE','LOCAL','VISITANTE') not null,
    cuota      numeric(8,2) not null,
    estado     enum ('ACERTADA','ANULADA','FALLADA','PENDIENTE') not null,
    constraint pk_seleccion primary key (id),
    constraint fk_seleccion_apuesta foreign key (apuesta_id) references apuesta,
    constraint fk_seleccion_evento foreign key (evento_id) references evento
);
