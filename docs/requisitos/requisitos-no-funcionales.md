# Requisitos no funcionales

Una **historia de usuario** describe algo que hace un actor en la web: apostar, consultar el ranking, crear una liga… Un **requisito no funcional** (RNF) describe *cómo* tiene que comportarse el sistema en conjunto: lo rápido que responde, lo seguro o lo accesible que es. No lo pide ni lo hace un actor concreto, así que no se escribe con el formato *Como… quiero… para…*.

Cada requisito tiene un identificador **RNF-xx** y sus criterios **RNF-xx.y**, que se comprueban con pruebas como los de las historias. La [matriz de trazabilidad](trazabilidad.md#rnf-01--tiempo-de-carga-requisito-no-funcional-antes-hu-85) enlaza cada criterio con su prueba.

## RNF-01 · Tiempo de carga

**Tipo:** rendimiento · **Fila del Product Backlog:** [41](../scrum/product-backlog.md) · **Estado:** hecho e integrado en `main` el 15/11 ([#102](https://github.com/riverri/IS1/pull/102)) · **Estimación:** 3 puntos, 246 líneas, 18 h sin IA, 3,1 h con IA, 2,9 $

**Qué se pide:** las páginas tienen que abrirse enseguida, también la primera vez después de un rato sin que nadie entre en la web.

| ID | Criterio | Prueba |
|---|---|---|
| RNF-01.1 | Tras 15 minutos sin visitas, la web se abre sin esperar a que el servidor vuelva a arrancar: no se apaga por falta de visitas | `KeepAliveTest`, `RendimientoWebTest.elPingRespondeSinIniciarSesion`, `PingSinBaseDeDatosTest` |
| RNF-01.2 | Si el navegador acepta páginas comprimidas, le llegan comprimidas (se descarga menos); si no, le llegan sin comprimir | `CompresionTest` |
| RNF-01.3 | El navegador no vuelve a descargar la hoja de estilos al cambiar de página, salvo que haya cambiado | `RendimientoWebTest.laHojaDeEstilosLlevaSuHuellaYElNavegadorLaGuardaUnAnio` |

**Cómo se cumple:**
- **Keep-alive:** el plan gratuito de Render apaga la web tras 15 minutos sin peticiones. Para evitarlo, la propia aplicación se visita a sí misma (`GET /ping`) cada 10 minutos. `/ping` responde sin sesión, sin base de datos y sin plantilla.
- **Compresión:** las páginas, los estilos y el JSON viajan comprimidos con gzip.
- **Caché:** la hoja de estilos lleva la huella de su contenido en el nombre y el navegador la guarda un año. Si cambia, cambia el nombre y se descarga la nueva.

**Medido en local antes y después del cambio:** la portada pasa de 4,5 KB a 1,5 KB, la lista de equipos de 122 KB a 6,5 KB y la hoja de estilos de 50 KB a 10 KB, que además se descarga una sola vez. Antes, la primera visita tras 15 minutos sin uso esperaba de 30 a 60 segundos a que Render volviera a arrancar la web.

**Historia:** se pidió el 15/11 como historia de usuario, HU-85. El 27/11 se descartó como historia, porque no lo es, y pasó a ser este requisito. El número HU-85 se queda como hueco en las [historias de usuario](historias-de-usuario.md) para no cambiar la numeración.
