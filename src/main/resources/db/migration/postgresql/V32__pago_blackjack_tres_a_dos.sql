-- Restablece el pago clásico al actualizar. Después, Gestión puede volver a cambiarlo para manos nuevas.
update configuracion_casino set pago_blackjack = 'TRES_A_DOS' where id = 1;
