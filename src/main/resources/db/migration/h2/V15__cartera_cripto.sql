-- V15: cartera de criptomonedas con precios reales, compradas y vendidas con moneditas (HU-69 a HU-73).
-- 1 monedita = 1 euro. Solo el precio es real: nadie tiene criptomonedas de verdad.

alter table movimiento_saldo alter column tipo set data type
    enum ('APUESTA','BAJA','BIENVENIDA','CASINO_APUESTA','CASINO_PREMIO','CORRECCION','CRIPTO_COMPRA','CRIPTO_VENTA',
          'DEVOLUCION','PREDICCION_COMPRA','PREDICCION_PAGO','PREDICCION_VENTA','PREMIO','RECARGA','SALDO_INICIAL');

alter table anuncio_ubicacion alter column ubicacion set data type
    enum ('CASINO','CRIPTO','CUENTA','EQUIPOS','EVENTOS','LIGAS','MERCADOS','MIS_APUESTAS','PORTADA','PREDICCIONES',
          'RANKING');

create sequence criptomoneda_seq start with 1 increment by 50;
create sequence precio_cripto_seq start with 1 increment by 50;
create sequence posicion_cripto_seq start with 1 increment by 50;
create sequence operacion_cripto_seq start with 1 increment by 50;

create table configuracion_cripto (
    id              bigint       not null,
    activo          boolean      not null,
    comision        numeric(5,4) not null,
    minutos_validez integer      not null,
    constraint pk_configuracion_cripto primary key (id)
);

-- Activa, 1 % de comisión y precios que sirven 15 minutos
insert into configuracion_cripto (id, activo, comision, minutos_validez) values (1, true, 0.0100, 15);

create table criptomoneda (
    id           bigint         not null,
    simbolo      varchar(10)    not null,
    nombre       varchar(60)    not null,
    id_api       varchar(60)    not null,
    categoria    enum ('MEMECOIN','REFERENCIA') not null,
    activa       boolean        not null,
    orden        integer        not null,
    precio       numeric(30,12),
    variacion24h numeric(10,4),
    actualizado  timestamp(6),
    constraint pk_criptomoneda primary key (id),
    constraint uk_criptomoneda_simbolo unique (simbolo),
    constraint uk_criptomoneda_id_api unique (id_api)
);

create table precio_cripto (
    id        bigint         not null,
    cripto_id bigint         not null,
    precio    numeric(30,12) not null,
    fecha     timestamp(6)   not null,
    constraint pk_precio_cripto primary key (id),
    constraint fk_precio_cripto_cripto foreign key (cripto_id) references criptomoneda
);

create index ix_precio_cripto_fecha on precio_cripto (cripto_id, fecha);

create table posicion_cripto (
    id         bigint         not null,
    usuario_id bigint         not null,
    cripto_id  bigint         not null,
    cantidad   numeric(38,12) not null,
    coste      numeric(14,2)  not null,
    constraint pk_posicion_cripto primary key (id),
    constraint uk_posicion_cripto unique (usuario_id, cripto_id),
    constraint fk_posicion_cripto_usuario foreign key (usuario_id) references usuario,
    constraint fk_posicion_cripto_cripto foreign key (cripto_id) references criptomoneda
);

create table operacion_cripto (
    id         bigint         not null,
    usuario_id bigint         not null,
    cripto_id  bigint         not null,
    tipo       enum ('COMPRA','VENTA') not null,
    cantidad   numeric(38,12) not null,
    precio     numeric(30,12) not null,
    importe    numeric(14,2)  not null,
    comision   numeric(14,2)  not null,
    fecha      timestamp(6)   not null,
    constraint pk_operacion_cripto primary key (id),
    constraint fk_operacion_cripto_usuario foreign key (usuario_id) references usuario,
    constraint fk_operacion_cripto_cripto foreign key (cripto_id) references criptomoneda
);

create index ix_operacion_cripto_usuario on operacion_cripto (usuario_id, fecha);

-- Criptomonedas de partida, con su identificador en CoinGecko. Las memecoins son las protagonistas.
-- El precio llega con la primera descarga; el creador puede quitar cualquiera en Gestión → Criptomonedas.
insert into criptomoneda (id, simbolo, nombre, id_api, categoria, activa, orden) values
    (next value for criptomoneda_seq, 'DOGE', 'Dogecoin', 'dogecoin', 'MEMECOIN', true, 1),
    (next value for criptomoneda_seq, 'SHIB', 'Shiba Inu', 'shiba-inu', 'MEMECOIN', true, 2),
    (next value for criptomoneda_seq, 'PEPE', 'Pepe', 'pepe', 'MEMECOIN', true, 3),
    (next value for criptomoneda_seq, 'WIF', 'dogwifhat', 'dogwifcoin', 'MEMECOIN', true, 4),
    (next value for criptomoneda_seq, 'BONK', 'Bonk', 'bonk', 'MEMECOIN', true, 5),
    (next value for criptomoneda_seq, 'FLOKI', 'Floki', 'floki', 'MEMECOIN', true, 6),
    (next value for criptomoneda_seq, 'FARTCOIN', 'Fartcoin', 'fartcoin', 'MEMECOIN', true, 7),
    (next value for criptomoneda_seq, 'BTC', 'Bitcoin', 'bitcoin', 'REFERENCIA', true, 1),
    (next value for criptomoneda_seq, 'ETH', 'Ethereum', 'ethereum', 'REFERENCIA', true, 2),
    (next value for criptomoneda_seq, 'SOL', 'Solana', 'solana', 'REFERENCIA', true, 3);

insert into anuncio (id, titulo, texto, icono, destino, prioridad, activo, impresiones) values
    (next value for anuncio_seq, 'Memecoins con moneditas',
     'DOGE, PEPE, BONK… a su precio real en euros. Sube como un cohete o se hunde en horas: tú decides.', '🐸',
     '/cripto', 5, true, 0);
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'PORTADA' from anuncio where destino = '/cripto';
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'CASINO' from anuncio where destino = '/cripto';
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'PREDICCIONES' from anuncio where destino = '/cripto';
