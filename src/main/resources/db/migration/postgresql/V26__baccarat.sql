-- V26: baccarat en el casino (HU-74). Es otro juego de la misma tabla partida.
-- La restricción la creó V13 con el nombre automático
alter table partida drop constraint partida_juego_check;
alter table partida add constraint partida_juego_check check (juego in ('RULETA', 'BLACKJACK', 'BACCARAT'));
