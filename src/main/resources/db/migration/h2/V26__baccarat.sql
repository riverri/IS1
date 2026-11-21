-- V26: baccarat en el casino (HU-74). Es otro juego de la misma tabla partida.
alter table partida alter column juego set data type enum ('BACCARAT','BLACKJACK','RULETA');
