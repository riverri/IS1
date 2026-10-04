-- V9: control de concurrencia sobre el saldo y origen de las suspensiones de los eventos.

-- Versión del usuario: si dos operaciones cambian el saldo a la vez, la segunda falla en lugar de pisar a la primera
alter table usuario add column version bigint default 0 not null;

-- Si lo suspendió la sincronización con la API (aplazado); los que suspende el creador no los reactiva la API
alter table evento add column suspendido_por_api boolean default false not null;
