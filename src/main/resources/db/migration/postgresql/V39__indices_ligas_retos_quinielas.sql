-- Claves ajenas que se consultan en cada petición y que PostgreSQL no indexa solo (H2 sí). Medido con 105.000
-- miembros de liga, 50.000 retos, 20.000 supercuotas y 1.000 quinielas: las ligas y los compañeros de un usuario
-- pasan de 4-6 ms a menos de 1 ms, sus retos de 3,7 a 0,1 ms, las supercuotas del catálogo de 1,8 a 0,2 ms y los
-- partidos de una página de quinielas de 1,3 a 0,2 ms. No se indexan candidato, equipo_competiciones ni
-- evento(competicion_id): con esos datos no cambiaba nada.
create index if not exists ix_liga_miembro_usuario on liga_miembro (usuario_id);
create index if not exists ix_reto_retador on reto (retador_id);
create index if not exists ix_reto_retado on reto (retado_id);
create index if not exists ix_supercuota_evento on supercuota (evento_id);
create index if not exists ix_partido_quiniela_quiniela on partido_quiniela (quiniela_id);
