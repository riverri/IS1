create sequence cambio_correo_pendiente_seq start with 1 increment by 50;

create table cambio_correo_pendiente (
    id          bigint        not null,
    usuario_id  bigint        not null,
    hash        varchar(64)   not null,
    nuevo_email varchar(254)  not null,
    caduca      timestamp     not null,
    usado       boolean       not null,
    constraint pk_cambio_correo_pendiente primary key (id),
    constraint uk_cambio_correo_pendiente_hash unique (hash),
    constraint fk_cambio_correo_pendiente_usuario foreign key (usuario_id) references usuario
);

create index ix_cambio_correo_pendiente_usuario on cambio_correo_pendiente (usuario_id);
