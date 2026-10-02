-- V6 (Sprint 9): el creador puede activar o desactivar el juego responsable en toda la web (HU-10).
alter table limites add column juego_responsable boolean default true not null;
