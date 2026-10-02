-- V5 (Sprint 9): juego responsable (HU-10) y eliminación de cuentas (HU-18).

-- Límites que se pone cada usuario: importe máximo apostado en 24 horas y en 7 días (null = sin límite)
alter table usuario add column limite_diario numeric(12,2);
alter table usuario add column limite_semanal numeric(12,2);
-- Pausa temporal: no puede apostar hasta esta fecha
alter table usuario add column pausa_hasta timestamp(6);
-- Cuentas eliminadas: se anonimizan y se conservan para que cuadren las apuestas ya hechas
alter table usuario add column eliminado boolean default false not null;
