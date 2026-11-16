-- V16: correo al ganar (HU-77), alerta de riesgo por evento (HU-76) y quiniela (HU-78 a HU-80).

-- HU-77: cada usuario puede desactivar los correos de premios en Mi cuenta; por defecto, activados
alter table usuario add column correos_activados boolean default true not null;

-- HU-76: si en el peor resultado la casa perdería más que esto, el evento sale en alerta
alter table limites add column alerta_riesgo numeric(12,2) default 2000 not null;

-- Movimientos del saldo de la quiniela: el boleto cuenta como lo jugado en el juego responsable
alter table movimiento_saldo alter column tipo set data type
    enum ('APUESTA','BAJA','BIENVENIDA','CASINO_APUESTA','CASINO_PREMIO','CORRECCION','CRIPTO_COMPRA','CRIPTO_VENTA',
          'DEVOLUCION','PREDICCION_COMPRA','PREDICCION_PAGO','PREDICCION_VENTA','PREMIO','QUINIELA_BOLETO',
          'QUINIELA_PREMIO','RECARGA','SALDO_INICIAL');

create sequence quiniela_seq start with 1 increment by 50;
create sequence partido_quiniela_seq start with 1 increment by 50;
create sequence boleto_quiniela_seq start with 1 increment by 50;

create table quiniela (
    id       bigint        not null,
    version  bigint        not null,
    nombre   varchar(100)  not null,
    precio   numeric(12,2) not null,
    comision numeric(5,4)  not null,
    estado   enum ('ABIERTA','ANULADA','REPARTIDA') not null,
    creada   timestamp(6)  not null,
    constraint pk_quiniela primary key (id)
);

create table partido_quiniela (
    id          bigint  not null,
    quiniela_id bigint  not null,
    evento_id   bigint  not null,
    orden       integer not null,
    constraint pk_partido_quiniela primary key (id),
    constraint fk_partido_quiniela_quiniela foreign key (quiniela_id) references quiniela,
    constraint fk_partido_quiniela_evento foreign key (evento_id) references evento
);

create table boleto_quiniela (
    id          bigint        not null,
    quiniela_id bigint        not null,
    usuario_id  bigint        not null,
    pronosticos varchar(15)   not null,
    pagado      numeric(12,2) not null,
    aciertos    integer,
    premio      numeric(14,2) not null,
    devuelto    boolean       not null,
    fecha       timestamp(6)  not null,
    constraint pk_boleto_quiniela primary key (id),
    constraint uk_boleto_quiniela unique (quiniela_id, usuario_id),
    constraint fk_boleto_quiniela_quiniela foreign key (quiniela_id) references quiniela,
    constraint fk_boleto_quiniela_usuario foreign key (usuario_id) references usuario
);
