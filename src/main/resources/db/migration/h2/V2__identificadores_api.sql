-- V2: identificadores de la API de datos deportivos (Sprint 5).
-- "if not exists" porque quien ya arrancó la versión con la API puede tener las columnas creadas.

alter table equipo add column if not exists id_externo integer;
alter table evento add column if not exists id_externo bigint;

create unique index if not exists uk_equipo_id_externo on equipo (id_externo);
create unique index if not exists uk_evento_id_externo on evento (id_externo);
