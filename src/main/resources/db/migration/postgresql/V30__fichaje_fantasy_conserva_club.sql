-- V30: cada fichaje de la liga fantasy guarda el club, la posición y el nombre que tenía el jugador al ficharlo.
-- Antes salían del jugador en cada momento: si la API lo traspasaba a otro club o le cambiaba la posición, sus
-- puntos pasados se recalculaban con el club nuevo y la formación se rompía; y si se borraba al jugador, se borraban
-- sus fichajes y con ellos sus puntos (CA-82.1: lo que ya sumó se queda). Ahora el fichaje se conserva sin jugador.
alter table fichaje_fantasy add column club_id bigint;
alter table fichaje_fantasy add column posicion varchar(20);
alter table fichaje_fantasy add column nombre_jugador varchar(255);

update fichaje_fantasy set club_id = (select j.equipo_id from jugador j where j.id = fichaje_fantasy.jugador_id),
                           posicion = (select j.posicion from jugador j where j.id = fichaje_fantasy.jugador_id),
                           nombre_jugador = (select j.nombre from jugador j where j.id = fichaje_fantasy.jugador_id);

alter table fichaje_fantasy alter column club_id set not null;
alter table fichaje_fantasy alter column posicion set not null;
alter table fichaje_fantasy alter column nombre_jugador set not null;
alter table fichaje_fantasy add constraint fk_fichaje_fantasy_club foreign key (club_id) references equipo;
alter table fichaje_fantasy add constraint ck_fichaje_fantasy_posicion
    check (posicion in ('PORTERO', 'DEFENSA', 'CENTROCAMPISTA', 'DELANTERO'));

alter table fichaje_fantasy drop constraint fk_fichaje_fantasy_jugador;
alter table fichaje_fantasy alter column jugador_id drop not null;
alter table fichaje_fantasy add constraint fk_fichaje_fantasy_jugador
    foreign key (jugador_id) references jugador on delete set null;
