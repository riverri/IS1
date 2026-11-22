-- PostgreSQL no crea índices para las claves ajenas (H2 sí). Sin ellos, cargar las selecciones de una página de
-- apuestas, el volumen de cada partido (cuotas) y la clasificación fantasy recorren tablas enteras.
create index if not exists ix_seleccion_apuesta on seleccion (apuesta_id);
create index if not exists ix_evento_local on evento (local_id, estado, fecha_hora);
create index if not exists ix_evento_visitante on evento (visitante_id, estado, fecha_hora);
