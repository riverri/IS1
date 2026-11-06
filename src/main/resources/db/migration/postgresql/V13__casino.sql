-- V13: casino con ruleta europea y blackjack (HU-56 a HU-59).

-- Nuevos tipos de movimiento del saldo (la restricción la creó V11 con el nombre automático)
alter table movimiento_saldo drop constraint movimiento_saldo_tipo_check;
alter table movimiento_saldo add constraint movimiento_saldo_tipo_check
    check (tipo in ('BIENVENIDA', 'RECARGA', 'APUESTA', 'PREMIO', 'DEVOLUCION', 'CORRECCION', 'BAJA', 'SALDO_INICIAL',
                    'CASINO_APUESTA', 'CASINO_PREMIO'));

-- El casino como sección donde salen anuncios y a la que pueden llevar
alter table anuncio_ubicacion drop constraint anuncio_ubicacion_ubicacion_check;
alter table anuncio_ubicacion add constraint anuncio_ubicacion_ubicacion_check
    check (ubicacion in ('PORTADA', 'EVENTOS', 'MERCADOS', 'EQUIPOS', 'RANKING', 'MIS_APUESTAS', 'LIGAS', 'CUENTA',
                         'CASINO'));

-- Configuración del casino: una sola fila, como la de límites
create table configuracion_casino (
    id                      bigint        not null,
    activo                  boolean       not null,
    importe_minimo          numeric(12,2) not null,
    importe_maximo          numeric(12,2) not null,
    barajas                 integer       not null,
    banca_pide_con17_blando boolean       not null,
    pago_blackjack          varchar(255)  not null check (pago_blackjack in ('TRES_A_DOS', 'SEIS_A_CINCO')),
    constraint pk_configuracion_casino primary key (id)
);

insert into configuracion_casino (id, activo, importe_minimo, importe_maximo, barajas, banca_pide_con17_blando, pago_blackjack)
values (1, true, 1.00, 200.00, 6, false, 'TRES_A_DOS');

create sequence partida_seq start with 1 increment by 50;

create table partida (
    id           bigint        not null,
    version      bigint        not null,
    usuario_id   bigint        not null,
    juego        varchar(255)  not null check (juego in ('RULETA', 'BLACKJACK')),
    apostado     numeric(12,2) not null,
    premio       numeric(14,2) not null,
    estado       varchar(255)  not null check (estado in ('EN_CURSO', 'TERMINADA')),
    apuesta      varchar(100)  not null,
    resultado    varchar(200),
    estado_juego varchar(2000),
    fecha        timestamp(6)  not null,
    constraint pk_partida primary key (id),
    constraint fk_partida_usuario foreign key (usuario_id) references usuario
);

create index ix_partida_usuario on partida (usuario_id, fecha);

-- Anuncio de partida del casino (lleva a una sección con sesión: no lo ven los visitantes)
insert into anuncio (id, titulo, texto, icono, destino, prioridad, activo, impresiones) values
    (nextval('anuncio_seq'), 'Prueba el casino',
     'Ruleta europea y blackjack con tus moneditas. Juega con cabeza: tus límites también cuentan aquí.', '🎰',
     '/casino', 5, true, 0);
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'PORTADA' from anuncio where destino = '/casino';
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'MIS_APUESTAS' from anuncio where destino = '/casino';
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'CUENTA' from anuncio where destino = '/casino';
