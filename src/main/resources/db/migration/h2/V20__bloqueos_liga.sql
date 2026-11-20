-- Bloqueos entre compañeros de una liga (HU-105).
alter table movimiento_saldo alter column tipo set data type
    enum ('APUESTA','BAJA','BIENVENIDA','BLOQUEO_LIGA','CASINO_APUESTA','CASINO_PREMIO','COBRO_ANTICIPADO',
          'CORRECCION','CRIPTO_COMPRA','CRIPTO_VENTA','DEVOLUCION','PREDICCION_COMPRA','PREDICCION_PAGO',
          'PREDICCION_VENTA','PREMIO','QUINIELA_BOLETO','QUINIELA_PREMIO','RECARGA','RETO_APUESTA',
          'RETO_DEVOLUCION','RETO_PREMIO','SALDO_INICIAL');

create sequence bloqueo_liga_seq start with 1 increment by 50;

create table bloqueo_liga (
    id          bigint       not null,
    liga_id     bigint       not null,
    creador_id  bigint       not null,
    objetivo_id bigint       not null,
    evento_id   bigint       not null,
    alcance     varchar(20)  not null,
    creada      timestamp(6) not null,
    constraint pk_bloqueo_liga primary key (id),
    constraint fk_bloqueo_liga_liga foreign key (liga_id) references liga on delete cascade,
    constraint fk_bloqueo_liga_creador foreign key (creador_id) references usuario on delete cascade,
    constraint fk_bloqueo_liga_objetivo foreign key (objetivo_id) references usuario on delete cascade,
    constraint fk_bloqueo_liga_evento foreign key (evento_id) references evento on delete cascade,
    constraint uk_bloqueo_liga_alcance unique (liga_id, objetivo_id, evento_id, alcance)
);
