-- Acelera el catálogo y la portada, que buscan eventos programados por fecha.
create index ix_evento_estado_fecha on evento (estado, fecha_hora);
