-- V11: libro de movimientos del saldo (HU-55). Cada cambio del saldo deja una fila con su motivo.

create sequence movimiento_saldo_seq start with 1 increment by 50;

create table movimiento_saldo (
    id               bigint        not null,
    usuario_id       bigint        not null,
    tipo             enum ('APUESTA','BAJA','BIENVENIDA','CORRECCION','DEVOLUCION','PREMIO','RECARGA','SALDO_INICIAL') not null,
    importe          numeric(14,2) not null,
    saldo_resultante numeric(14,2) not null,
    concepto         varchar(200)  not null,
    fecha            timestamp(6)  not null,
    constraint pk_movimiento_saldo primary key (id),
    constraint fk_movimiento_saldo_usuario foreign key (usuario_id) references usuario
);

create index ix_movimiento_saldo_usuario on movimiento_saldo (usuario_id, fecha);

-- Las cuentas que ya existían empiezan el libro con su saldo actual, para que el último saldo resultante
-- coincida siempre con el saldo (CA-55.2). Las eliminadas no tienen saldo que explicar.
insert into movimiento_saldo (id, usuario_id, tipo, importe, saldo_resultante, concepto, fecha)
select next value for movimiento_saldo_seq, id, 'SALDO_INICIAL', saldo, saldo, 'Saldo anterior a los movimientos',
       localtimestamp
from usuario
where eliminado = false and saldo <> 0;
