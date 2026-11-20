-- Fecha del abono anticipado para mostrar el cierre en el historial (HU-104).
alter table apuesta add column fecha_cobro_anticipado timestamp(6);
