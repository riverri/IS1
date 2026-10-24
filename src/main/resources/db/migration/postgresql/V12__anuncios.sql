-- V12: anuncios internos que promocionan otras secciones (HU-60, HU-61) y sus clics (HU-62).

create sequence anuncio_seq start with 1 increment by 50;
create sequence clic_anuncio_seq start with 1 increment by 50;

create table anuncio (
    id          bigint       not null,
    titulo      varchar(60)  not null,
    texto       varchar(160) not null,
    icono       varchar(8),
    destino     varchar(200) not null,
    prioridad   integer      not null,
    activo      boolean      not null,
    desde       timestamp(6),
    hasta       timestamp(6),
    impresiones bigint       not null,
    constraint pk_anuncio primary key (id)
);

create table anuncio_ubicacion (
    anuncio_id bigint not null,
    ubicacion  varchar(255) not null check (ubicacion in ('PORTADA', 'EVENTOS', 'MERCADOS', 'EQUIPOS', 'RANKING',
                                                       'MIS_APUESTAS', 'LIGAS', 'CUENTA')),
    constraint pk_anuncio_ubicacion primary key (anuncio_id, ubicacion),
    constraint fk_anuncio_ubicacion_anuncio foreign key (anuncio_id) references anuncio
);

create table clic_anuncio (
    id         bigint       not null,
    anuncio_id bigint       not null,
    usuario_id bigint,
    origen     varchar(200) not null,
    fecha      timestamp(6) not null,
    constraint pk_clic_anuncio primary key (id),
    constraint fk_clic_anuncio_anuncio foreign key (anuncio_id) references anuncio,
    constraint fk_clic_anuncio_usuario foreign key (usuario_id) references usuario
);

create index ix_clic_anuncio_anuncio on clic_anuncio (anuncio_id);

-- Anuncios de partida, de las secciones que ya existen. El creador los cambia en Gestión → Anuncios
insert into anuncio (id, titulo, texto, icono, destino, prioridad, activo, impresiones) values
    (nextval('anuncio_seq'), 'Apuestas a largo plazo',
     '¿Quién ganará el Balón de Oro o LaLiga? Apuesta ya a los mercados de toda la temporada.', '🏆', '/mercados', 5, true, 0),
    (nextval('anuncio_seq'), 'Compite con tus amigos',
     'Crea una liga privada, comparte el código y mirad quién va primero.', '👥', '/ligas', 5, true, 0),
    (nextval('anuncio_seq'), '¿Quién va primero?',
     'Mira el ranking de jugadores y compara tus aciertos con los de los demás.', '📊', '/ranking', 3, true, 0),
    (nextval('anuncio_seq'), 'Antes de apostar, mira las fichas',
     'Últimos resultados, racha y alineación probable de cada equipo.', '📋', '/equipos', 2, true, 0);

insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'PORTADA' from anuncio where destino in ('/mercados', '/ligas');
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'EVENTOS' from anuncio where destino in ('/mercados', '/ranking', '/equipos');
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'MIS_APUESTAS' from anuncio where destino in ('/mercados', '/ligas', '/ranking');
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'MERCADOS' from anuncio where destino in ('/ranking', '/ligas');
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'RANKING' from anuncio where destino = '/ligas';
insert into anuncio_ubicacion (anuncio_id, ubicacion)
select id, 'CUENTA' from anuncio where destino in ('/ligas', '/ranking');
