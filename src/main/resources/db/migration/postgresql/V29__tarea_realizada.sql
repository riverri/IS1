-- V29: cuándo se hizo por última vez una tarea programada. La usa el resumen semanal (HU-92): si la aplicación
-- estaba parada el lunes a las 9:00, lo manda en cuanto vuelve a arrancar en vez de perder esa semana.
create table tarea_realizada (
    nombre varchar(60)  primary key,
    ultima timestamp(6) not null
);
