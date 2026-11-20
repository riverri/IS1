-- Un bloqueo de alcance liga no tiene una persona objetivo; la clave impide crear duplicados globales.
alter table bloqueo_liga alter column objetivo_id drop not null;
alter table bloqueo_liga add column destinatario_key varchar(50);
update bloqueo_liga set destinatario_key = 'AMIGO:' || objetivo_id::text;
alter table bloqueo_liga alter column destinatario_key set not null;
alter table bloqueo_liga add constraint uk_bloqueo_liga_destinatario
    unique (liga_id, evento_id, alcance, destinatario_key);
