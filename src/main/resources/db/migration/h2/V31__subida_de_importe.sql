-- Las subidas de importe de una apuesta cuentan en el juego responsable cuando se hacen (HU-10, HU-27):
-- tienen su propio tipo de movimiento, y cada apuesta guarda lo que se apostó al hacerla.
alter table movimiento_saldo alter column tipo set data type
    enum ('APUESTA','BAJA','BIENVENIDA','BLOQUEO_LIGA','CASINO_APUESTA','CASINO_PREMIO','COBRO_ANTICIPADO',
          'CORRECCION','CRIPTO_COMPRA','CRIPTO_VENTA','DEVOLUCION','PREDICCION_COMPRA','PREDICCION_PAGO',
          'PREDICCION_VENTA','PREMIO','QUINIELA_BOLETO','QUINIELA_PREMIO','RECARGA','RETO_APUESTA',
          'RETO_DEVOLUCION','RETO_PREMIO','SALDO_INICIAL','SUBIDA_APUESTA');

alter table apuesta add column importe_inicial numeric(12, 2);
update apuesta set importe_inicial = importe;
alter table apuesta alter column importe_inicial set not null;
