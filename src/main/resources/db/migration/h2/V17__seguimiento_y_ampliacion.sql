-- V17: historias HU-75 y HU-86 a HU-96 del Sprint 4.

-- HU-86: equipos favoritos
create sequence equipo_favorito_seq start with 1 increment by 50;
create table equipo_favorito (
    id         bigint not null,
    usuario_id bigint not null,
    equipo_id  bigint not null,
    constraint pk_equipo_favorito primary key (id),
    constraint uk_equipo_favorito unique (usuario_id, equipo_id),
    constraint fk_equipo_favorito_usuario foreign key (usuario_id) references usuario,
    constraint fk_equipo_favorito_equipo foreign key (equipo_id) references equipo
);

-- HU-90: alertas de cuota
create sequence alerta_cuota_seq start with 1 increment by 50;
create table alerta_cuota (
    id         bigint        not null,
    usuario_id bigint        not null,
    evento_id  bigint        not null,
    resultado  enum ('EMPATE','LOCAL','VISITANTE') not null,
    cuota      numeric(8,2)  not null,
    creada     timestamp(6)  not null,
    avisada    timestamp(6),
    constraint pk_alerta_cuota primary key (id),
    constraint fk_alerta_cuota_usuario foreign key (usuario_id) references usuario,
    constraint fk_alerta_cuota_evento foreign key (evento_id) references evento
);

-- HU-91: evolución de las cuotas
create sequence cuota_historica_seq start with 1 increment by 50;
create table cuota_historica (
    id        bigint       not null,
    evento_id bigint       not null,
    fecha     timestamp(6) not null,
    cuota_local     numeric(8,2) not null,
    cuota_empate    numeric(8,2),
    cuota_visitante numeric(8,2) not null,
    constraint pk_cuota_historica primary key (id),
    constraint fk_cuota_historica_evento foreign key (evento_id) references evento
);

-- HU-87: supercuotas
create sequence supercuota_seq start with 1 increment by 50;
create table supercuota (
    id             bigint        not null,
    evento_id      bigint        not null,
    resultado      enum ('EMPATE','LOCAL','VISITANTE') not null,
    cuota          numeric(8,2)  not null,
    importe_maximo numeric(12,2) not null,
    hasta          timestamp(6)  not null,
    creada         timestamp(6)  not null,
    constraint pk_supercuota primary key (id),
    constraint fk_supercuota_evento foreign key (evento_id) references evento
);

-- HU-92: resumen semanal por correo; por defecto, activado
alter table usuario add column resumen_semanal boolean default true not null;

-- HU-75: apuestas cobradas antes de tiempo (cash-out)
alter table apuesta alter column estado set data type
    enum ('ACTIVA','ANULADA','CANCELADA','COBRADA','GANADA','PERDIDA');

-- Movimientos nuevos: cash-out (HU-75) y retos 1 contra 1 (HU-89)
alter table movimiento_saldo alter column tipo set data type
    enum ('APUESTA','BAJA','BIENVENIDA','CASINO_APUESTA','CASINO_PREMIO','COBRO_ANTICIPADO','CORRECCION','CRIPTO_COMPRA',
          'CRIPTO_VENTA','DEVOLUCION','PREDICCION_COMPRA','PREDICCION_PAGO','PREDICCION_VENTA','PREMIO','QUINIELA_BOLETO',
          'QUINIELA_PREMIO','RECARGA','RETO_APUESTA','RETO_DEVOLUCION','RETO_PREMIO','SALDO_INICIAL');

-- HU-89: retos 1 contra 1
create sequence reto_seq start with 1 increment by 50;
create table reto (
    id                 bigint        not null,
    version            bigint        not null,
    retador_id         bigint        not null,
    retado_id          bigint        not null,
    evento_id          bigint        not null,
    pronostico_retador enum ('EMPATE','LOCAL','VISITANTE') not null,
    pronostico_retado  enum ('EMPATE','LOCAL','VISITANTE'),
    importe            numeric(12,2) not null,
    estado             enum ('ACEPTADO','DEVUELTO','PENDIENTE','RECHAZADO','RESUELTO') not null,
    ganador_id         bigint,
    creado             timestamp(6)  not null,
    constraint pk_reto primary key (id),
    constraint fk_reto_retador foreign key (retador_id) references usuario,
    constraint fk_reto_retado foreign key (retado_id) references usuario,
    constraint fk_reto_evento foreign key (evento_id) references evento,
    constraint fk_reto_ganador foreign key (ganador_id) references usuario
);
