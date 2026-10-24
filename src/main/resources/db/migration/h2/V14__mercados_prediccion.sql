-- V14: mercados de predicción de sí o no con creador de mercado LMSR (HU-63 a HU-68).

alter table movimiento_saldo alter column tipo set data type
    enum ('APUESTA','BAJA','BIENVENIDA','CASINO_APUESTA','CASINO_PREMIO','CORRECCION','DEVOLUCION','PREDICCION_COMPRA',
          'PREDICCION_PAGO','PREDICCION_VENTA','PREMIO','RECARGA','SALDO_INICIAL');

alter table anuncio_ubicacion alter column ubicacion set data type
    enum ('CASINO','CUENTA','EQUIPOS','EVENTOS','LIGAS','MERCADOS','MIS_APUESTAS','PORTADA','PREDICCIONES','RANKING');

create sequence mercado_prediccion_seq start with 1 increment by 50;
create sequence posicion_prediccion_seq start with 1 increment by 50;
create sequence operacion_prediccion_seq start with 1 increment by 50;

create table mercado_prediccion (
    id          bigint        not null,
    version     bigint        not null,
    pregunta    varchar(200)  not null,
    descripcion varchar(500),
    cierre      timestamp(6)  not null,
    estado      enum ('ABIERTO','ANULADO','CERRADO','RESUELTO') not null,
    resultado   enum ('NO','SI'),
    liquidez    numeric(12,2) not null,
    comision    numeric(5,4)  not null,
    q_si        numeric(20,6) not null,
    q_no        numeric(20,6) not null,
    creado      timestamp(6)  not null,
    constraint pk_mercado_prediccion primary key (id)
);

create table posicion_prediccion (
    id              bigint        not null,
    usuario_id      bigint        not null,
    mercado_id      bigint        not null,
    lado            enum ('NO','SI') not null,
    participaciones numeric(20,6) not null,
    invertido       numeric(14,2) not null,
    pagado          numeric(14,2) not null,
    constraint pk_posicion_prediccion primary key (id),
    constraint uk_posicion_prediccion unique (usuario_id, mercado_id, lado),
    constraint fk_posicion_prediccion_usuario foreign key (usuario_id) references usuario,
    constraint fk_posicion_prediccion_mercado foreign key (mercado_id) references mercado_prediccion
);

create table operacion_prediccion (
    id                bigint        not null,
    usuario_id        bigint        not null,
    mercado_id        bigint        not null,
    lado              enum ('NO','SI') not null,
    tipo              enum ('COMPRA','VENTA') not null,
    participaciones   numeric(20,6) not null,
    importe           numeric(14,2) not null,
    comision          numeric(14,2) not null,
    precio_si_despues numeric(7,4)  not null,
    fecha             timestamp(6)  not null,
    constraint pk_operacion_prediccion primary key (id),
    constraint fk_operacion_prediccion_usuario foreign key (usuario_id) references usuario,
    constraint fk_operacion_prediccion_mercado foreign key (mercado_id) references mercado_prediccion
);

create index ix_operacion_prediccion_mercado on operacion_prediccion (mercado_id, fecha);

-- Mercados de ejemplo: el creador los cambia o crea otros en Gestión → Predicciones
insert into mercado_prediccion (id, version, pregunta, descripcion, cierre, estado, liquidez, comision, q_si, q_no, creado)
values
    (next value for mercado_prediccion_seq, 0, '¿Ganará el FC Barcelona LaLiga 2026/27?',
     'Se resuelve con «Sí» si el FC Barcelona acaba primero en LaLiga 2026/27.', timestamp '2027-04-30 23:59:00',
     'ABIERTO', 100.00, 0.0200, 0, 0, localtimestamp),
    (next value for mercado_prediccion_seq, 0, '¿Llegará un equipo español a la final de la Champions 2026/27?',
     'Se resuelve con «Sí» si al menos un equipo de LaLiga juega la final.', timestamp '2027-05-01 23:59:00',
     'ABIERTO', 100.00, 0.0200, 0, 0, localtimestamp);

insert into anuncio (id, titulo, texto, icono, destino, prioridad, activo, impresiones) values
    (next value for anuncio_seq, 'Mercados de predicción',
     '¿Sí o no? Compra participaciones y mira qué piensa la comunidad: el precio es la probabilidad.', '📈',
     '/predicciones', 5, true, 0);
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'EVENTOS' from anuncio where destino = '/predicciones';
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'MERCADOS' from anuncio where destino = '/predicciones';
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'RANKING' from anuncio where destino = '/predicciones';
