-- Acelera los listados de apuestas activas e historial por usuario.
create index ix_apuesta_usuario_estado_fecha on apuesta (usuario_id, estado, fecha desc);
