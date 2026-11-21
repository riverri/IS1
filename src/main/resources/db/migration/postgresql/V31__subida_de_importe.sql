-- Las subidas de importe de una apuesta cuentan en el juego responsable cuando se hacen (HU-10, HU-27):
-- tienen su propio tipo de movimiento, y cada apuesta guarda lo que se apostó al hacerla.
alter table movimiento_saldo drop constraint movimiento_saldo_tipo_check;
alter table movimiento_saldo add constraint movimiento_saldo_tipo_check
    check (tipo in ('BIENVENIDA', 'RECARGA', 'APUESTA', 'PREMIO', 'DEVOLUCION', 'CORRECCION', 'BAJA', 'SALDO_INICIAL',
                    'CASINO_APUESTA', 'CASINO_PREMIO', 'PREDICCION_COMPRA', 'PREDICCION_VENTA', 'PREDICCION_PAGO',
                    'CRIPTO_COMPRA', 'CRIPTO_VENTA', 'QUINIELA_BOLETO', 'QUINIELA_PREMIO', 'COBRO_ANTICIPADO',
                    'RETO_APUESTA', 'RETO_PREMIO', 'RETO_DEVOLUCION', 'BLOQUEO_LIGA', 'SUBIDA_APUESTA'));

alter table apuesta add column importe_inicial numeric(12, 2);
update apuesta set importe_inicial = importe;
alter table apuesta alter column importe_inicial set not null;
