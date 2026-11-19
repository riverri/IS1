-- El creador ha cambiado a mano la fecha de un partido que viene de la API (por ejemplo, uno aplazado): la
-- sincronización ya no la cambia ni lo vuelve a suspender
alter table evento add column fecha_manual boolean default false not null;

-- Bloqueo optimista de las selecciones: cambiar el importe de una combinada a la vez que se anula uno de sus
-- partidos ya no vuelve a dejar pendiente la selección anulada
alter table seleccion add column version bigint default 0 not null;
