-- Los precios de las criptos se descargan cada 2 minutos y servían 15: si la descarga falla, quien viera el precio
-- real en otra web podría comprar sabiendo ya que ha subido. Ahora sirven 5 minutos (dos descargas fallidas
-- seguidas) y 30 como mucho, que es lo que admite ConfiguracionCripto.
update configuracion_cripto set minutos_validez = 5 where minutos_validez = 15;
update configuracion_cripto set minutos_validez = 30 where minutos_validez > 30;
