-- Depuración del Sprint 5

-- El resultado lo puso o corrigió el creador en Gestión: la sincronización con la API ya no lo cambia
alter table evento add column resultado_manual boolean default false not null;

-- Bloqueo optimista de las apuestas: cancelar y cambiar el importe a la vez ya no resucita una apuesta devuelta
alter table apuesta add column version bigint default 0 not null;
