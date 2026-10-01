# Historias de usuario

> Fuente: `Product_Backlog_IS1.pdf`. Formato de la asignatura: *Como [actor], quiero [acción] para [objetivo]*, con criterios de aceptación *Dado… cuando… entonces…*.
> Los IDs (HU-xx) sirven para referenciarlas desde los issues de GitHub. La columna **Fila** remite al [Product Backlog](../scrum/product-backlog.md).

## Actores
- **Visitante**: no ha iniciado sesión.
- **Usuario / apostador**: registrado; apuesta con moneditas virtuales.
- **Creador de apuestas** (administrador): gestiona equipos, eventos, cuotas, resultados y límites.

---

## Creador de apuestas

**HU-01**: Como creador de apuestas, quiero dar de alta competiciones, equipos y deportistas con su calificación de calidad (0–10), para que el sistema tenga la base con la que calcular las cuotas. *(Fila 1, 2)*
- Dado que doy de alta un equipo con su deporte, sus competiciones y su calificación, cuando lo guardo, entonces queda disponible para crear eventos con él.
- Dado que introduzco una calificación fuera del rango 0–10, cuando intento guardarla, entonces el sistema la rechaza.

**HU-02**: Como creador de apuestas, quiero ajustar manualmente el factor de forma reciente de un equipo (rachas, moral, lesiones…), para que las cuotas reflejen su momento actual y no solo su calidad general. *(Fila 3)*
- Dado que modifico el factor de forma de un equipo, cuando lo guardo, entonces se recalculan las cuotas de sus eventos que aún no han empezado.

**HU-03**: Como creador de apuestas, quiero que las cuotas iniciales (G/E/P) de cada evento se generen automáticamente con un margen para la casa, para no tener que fijarlas a mano. *(Fila 3)*
- Dado que creo un evento entre dos equipos registrados, cuando lo guardo, entonces el sistema propone una cuota para cada resultado a partir de su calidad y su forma.
- Dado que se generan o se recalculan las cuotas de un evento, entonces la suma de las probabilidades implícitas (1/cuota) es siempre mayor que 1, para que la casa no pierda a largo plazo.
- Dado que el volumen apostado hace bajar una cuota, entonces esta nunca baja de un mínimo fijado (p. ej. 1,01).

**HU-04**: Como creador de apuestas, quiero introducir el resultado final de un evento, para que se resuelvan todas las apuestas asociadas. *(Fila 16, 17)*
- Dado que un evento ha terminado, cuando introduzco su resultado y lo confirmo, entonces el evento pasa a "finalizado" y se lanza la resolución de sus apuestas.
- Dado que me he equivocado al introducir el resultado, cuando lo corrijo, entonces las apuestas se vuelven a resolver y los saldos se ajustan.

**HU-05**: Como creador de apuestas, quiero suspender o anular un evento (aplazamiento, error en las cuotas…), para que nadie apueste en condiciones incorrectas. *(Fila 17)*
- Dado que suspendo un evento, entonces deja de admitir apuestas nuevas hasta que lo reactive.
- Dado que anulo un evento, entonces se devuelve a cada usuario el importe que apostó en él.

**HU-06**: Como creador de apuestas, quiero ver cuánto se ha apostado en cada resultado de un evento, para comprobar que el ajuste de cuotas funciona y detectar desequilibrios. *(Fila 17)*
- Dado que entro en un evento desde el panel de gestión, entonces veo el importe total y el número de apuestas de cada resultado.

**HU-07**: Como creador de apuestas, quiero fijar límites de apuesta (importe mínimo y máximo, número máximo de selecciones en una combinada), para controlar el riesgo de la casa. *(Fila 17)*
- Dado que un usuario intenta apostar fuera de esos límites, cuando confirma la apuesta, entonces el sistema lo impide y le indica el límite.

## General

**HU-08**: Como visitante, quiero consultar el catálogo de eventos y sus cuotas sin estar registrado, para decidir si me interesa crear una cuenta. *(Fila 9)*
- Dado que no he iniciado sesión, cuando entro en la web, entonces puedo ver los eventos y sus cuotas.
- Dado que no he iniciado sesión, cuando intento apostar, entonces se me redirige al inicio de sesión o al registro.

**HU-09**: Como usuario, quiero que la web se adapte a móvil y a ordenador, para poder apostar desde cualquier dispositivo. *(Requisito no funcional)*
- Dado que accedo desde un móvil, cuando navego por la web, entonces todas las páginas se ven y se usan correctamente.

**HU-10**: Como usuario, quiero poder fijarme límites de apuesta (diarios o semanales) o una pausa temporal, para jugar de forma responsable. *(Fila 27)*
- Dado que he fijado un límite diario, cuando lo alcanzo, entonces no puedo apostar más hasta el día siguiente.
- Dado que activo una pausa, entonces no puedo apostar hasta que termine, aunque sí puedo consultar mi cuenta.

## Gestión de usuarios

**HU-11**: Como usuario, quiero registrarme y crear un perfil, para poder acceder a la aplicación con una cuenta propia. *(Fila 5)*
- Dado que relleno el formulario de registro con un email y una contraseña válidos, cuando lo envío, entonces se crea mi cuenta y mi perfil.
- Dado que intento registrarme con un email ya existente, cuando lo envío, entonces el sistema me avisa de que ya hay una cuenta con ese email.

**HU-12**: Como usuario, quiero iniciar sesión de forma segura, para proteger el acceso a mi cuenta y a mi saldo. *(Fila 5, 8)*
- Dado que introduzco mi email y contraseña correctos, cuando inicio sesión, entonces accedo a mi cuenta.
- Dado que los datos de acceso son incorrectos, cuando inicio sesión, entonces el sistema deniega el acceso con un mensaje de error genérico.

**HU-13**: Como usuario, quiero consultar mi saldo de moneditas virtuales en cualquier momento, para tener control de cuánto tengo disponible para apostar. *(Fila 4, 12)*
- Dado que soy un usuario registrado, cuando entro en mi cuenta, entonces veo mi saldo actual de moneditas.
- Dado que mi saldo cambia (apuesta o recarga), cuando lo vuelvo a consultar, entonces el valor está actualizado.

**HU-14**: Como usuario, quiero recibir una recarga inicial de moneditas al registrarme (y recargas periódicas gratuitas), para poder seguir jugando sin aportar dinero real. *(Fila 4)*
- Dado que completo el registro por primera vez, entonces se me asigna automáticamente el saldo de bienvenida.
- Dado que ha pasado el periodo definido desde mi última recarga gratuita, entonces recibo automáticamente una nueva recarga.

**HU-15**: Como usuario, quiero añadir y gestionar los datos de una cuenta bancaria (real o ficticia), para poder retirar mi saldo cuando lo desee. *(Fila 25)*
- Dado que no tengo ninguna cuenta bancaria vinculada, cuando añado una, entonces queda guardada asociada a mi perfil.
- Dado que ya tengo una cuenta vinculada, cuando la edito o la sustituyo por otra, entonces se actualiza la que se usará para futuras retiradas.

**HU-16**: Como usuario, quiero retirar mi saldo disponible a mi cuenta bancaria vinculada, para poder disponer de mis ganancias fuera de la aplicación. *(Fila 25)*
- Dado que tengo saldo disponible (no comprometido en apuestas activas) y una cuenta vinculada, cuando solicito una retirada por un importe igual o menor a ese saldo, entonces se descuenta de mi saldo y la solicitud queda registrada.
- Dado que solicito retirar más saldo del disponible, cuando confirmo la operación, entonces el sistema la rechaza.
- Dado que se completa una retirada, cuando consulto mi historial, entonces aparece registrada con su importe y fecha.

**HU-17**: Como usuario, quiero recuperar el acceso si olvido mi contraseña, para no perder mi cuenta ni mi saldo. *(Fila 22)*
- Dado que pulso "he olvidado mi contraseña" e introduzco mi email, cuando lo envío, entonces recibo un enlace para crear una contraseña nueva.
- Dado que el enlace ha caducado o ya se ha usado, cuando lo abro, entonces el sistema no permite cambiar la contraseña.

**HU-18**: Como usuario, quiero eliminar mi cuenta, para que se borren mis datos personales si dejo de usar la aplicación. *(Fila 22)*
- Dado que tengo apuestas activas, cuando intento eliminar la cuenta, entonces el sistema me avisa de que las perderé y me pide confirmación.
- Dado que confirmo la eliminación, entonces ya no puedo iniciar sesión y mis datos personales se borran.

## Catálogo de eventos deportivos

**HU-19**: Como usuario, quiero ver una lista de eventos deportivos disponibles organizados por deporte y fecha, para poder apostar a tiempo. *(Fila 9)*
- Dado que hay eventos disponibles, cuando entro en el catálogo, entonces los veo agrupados por deporte y ordenados por fecha.
- Dado que un evento ya ha empezado o terminado, cuando consulto el catálogo, entonces ya no aparece como disponible para apostar.

**HU-20**: Como usuario, quiero ver las cuotas asociadas a cada resultado posible de un evento, para apostar cuando el riesgo-recompensa compense. *(Fila 3, 9)*
- Dado que entro en un evento, cuando lo consulto, entonces veo una cuota para cada resultado posible (G/E/P).
- Dado que las cuotas cambian por el volumen apostado, cuando vuelvo a consultar el evento, entonces veo las cuotas actualizadas.

**HU-21**: Como administrador, quiero cargar o actualizar eventos y cuotas manualmente o desde una fuente externa, para corregir errores y mantener los datos al día. *(Fila 2, 13, 17)*
- Dado que doy de alta o edito un evento, cuando lo guardo, entonces se refleja de inmediato en el catálogo que ve el usuario.
- Dado que la fuente externa está disponible, cuando se sincroniza, entonces los eventos y cuotas se actualizan sin intervención manual. *(fuera del MVP)*

**HU-22**: Como usuario, quiero buscar un equipo o una competición y filtrar el catálogo, para encontrar rápido el evento que me interesa. *(Fila 21)*
- Dado que escribo el nombre de un equipo, cuando busco, entonces veo solo sus eventos disponibles.
- Dado que aplico filtros (deporte, competición, fecha), entonces el catálogo muestra solo los eventos que los cumplen.
- Dado que ningún evento cumple la búsqueda o los filtros, entonces se muestra un mensaje que lo indica.

## Apuesta simple

**HU-23**: Como usuario, quiero seleccionar un evento y una cuota y confirmar una apuesta con parte de mi saldo, para intentar ganar más moneditas si acierto el resultado. *(Fila 6)*
- Dado que tengo saldo suficiente, cuando confirmo una apuesta, entonces el importe se descuenta de mi saldo y la apuesta queda registrada como activa.
- Dado que no tengo saldo suficiente, cuando intento confirmar la apuesta, entonces el sistema me lo impide y me avisa.

**HU-24**: Como usuario, quiero ver el estado de mis apuestas activas, para saber en todo momento cuánto tengo comprometido y en qué apuestas. *(Fila 10, 11)*
- Dado que tengo apuestas activas, cuando entro en mi sección de apuestas, entonces veo cada una con su evento, cuota e importe.

**HU-25**: Como usuario, quiero que mis apuestas se resuelvan automáticamente en cuanto termina el evento, para recibir mis ganancias (o ver la pérdida) sin tener que reclamarlo yo mismo. *(Fila 16)*
- Dado que un evento pasa a "finalizado" con un resultado, entonces todas sus apuestas asociadas pasan a "ganada" o "perdida" según corresponda.
- Dado que una apuesta pasa a "ganada", entonces mi saldo se actualiza automáticamente con la ganancia.
- Dado que se resuelve una apuesta, cuando consulto mi historial, entonces veo el resultado sin haber hecho nada manualmente.

**HU-26**: Como usuario, quiero cancelar una apuesta activa antes de que empiece el evento, para recuperar el importe si cambio de opinión. *(Fila 14)*
- Dado que el evento aún no ha empezado, cuando cancelo la apuesta, entonces se me devuelve el importe al saldo.
- Dado que el evento ya ha empezado, cuando intento cancelarla, entonces el sistema no lo permite.

**HU-27**: Como usuario, quiero modificar el importe de una apuesta activa antes de que empiece el evento, para ajustar lo que arriesgo. *(Fila 14)*
- Dado que subo el importe y tengo saldo suficiente, cuando confirmo, entonces se descuenta la diferencia y se aplica la cuota vigente en ese momento.
- Dado que bajo el importe, cuando confirmo, entonces se me devuelve la diferencia al saldo.

## Apuestas combinadas

**HU-28**: Como usuario, quiero añadir a un mismo boleto selecciones de varios eventos, para crear mi propia apuesta combinada. *(Fila 15, 18)*
- Dado que tengo selecciones en el boleto, cuando añado otra de un evento distinto, entonces se añade al boleto.
- Dado que intento añadir dos selecciones del mismo evento, cuando las añado, entonces el sistema no lo permite y me avisa.
- Dado que quito una selección del boleto, entonces desaparece y, si solo queda una, el boleto pasa a ser una apuesta simple.

**HU-29**: Como usuario, quiero que el sistema calcule el multiplicador total del boleto y la ganancia potencial, para saber cuánto puedo ganar antes de confirmar. *(Fila 15, 18)*
- Dado que el boleto tiene varias selecciones, entonces el multiplicador total es el producto de las cuotas de todas ellas.
- Dado que introduzco un importe, entonces veo la ganancia potencial (importe × multiplicador total) antes de confirmar.
- Dado que cambia la cuota de alguna selección antes de confirmar, entonces el multiplicador se recalcula y se me pide que acepte la nueva cuota.

**HU-30**: Como usuario, quiero que mi combinada se resuelva según el resultado de todas sus selecciones, para cobrar solo si acierto todas. *(Fila 16)*
- Dado que falla una de las selecciones, entonces la combinada pasa a "perdida" sin esperar a que terminen los demás eventos.
- Dado que aciertan todas las selecciones, entonces la combinada pasa a "ganada" y se me abona la ganancia.
- Dado que uno de los eventos se anula, entonces esa selección cuenta con cuota 1,00 y el resto de la combinada sigue en juego.

## Banco de estadísticas de equipos

**HU-31**: Como usuario, quiero consultar la ficha de un equipo o deportista con sus últimos resultados y su posición en la clasificación, para valorar su forma antes de apostar. *(Fila 20)*
- Dado que entro en la ficha de un equipo, entonces veo sus últimos 5 resultados y su posición en cada competición.
- Dado que estoy viendo un evento, cuando pulso sobre uno de los equipos, entonces accedo a su ficha.

**HU-32**: Como usuario, quiero ver la evolución de un equipo a lo largo de la temporada en un gráfico, para detectar tendencias (rachas, mejora o bajón). *(Fila 20)*
- Dado que entro en la ficha de un equipo, cuando abro la pestaña de evolución, entonces veo un gráfico con sus puntos o su posición jornada a jornada.

**HU-33**: Como usuario, quiero comparar cara a cara a los dos rivales de un evento, para decidir mi apuesta con más información. *(Fila 20)*
- Dado que entro en un evento, cuando pulso "comparar", entonces veo el historial de enfrentamientos entre ambos y sus estadísticas principales una junto a otra.
- Dado que no hay enfrentamientos previos, entonces se indica y se muestran solo las estadísticas de cada uno.

## Historial y estadísticas personales

**HU-34**: Como usuario, quiero ver mi historial completo de apuestas con sus resultados, para poder repasar mis decisiones pasadas. *(Fila 19)*
- Dado que tengo apuestas resueltas, cuando entro en mi historial, entonces veo cada una con su evento, cuota, importe y resultado.

**HU-35**: Como usuario, quiero ver estadísticas básicas de mi actividad (porcentaje de aciertos, rentabilidad), para saber si mi balance global es positivo o negativo. *(Fila 19)*
- Dado que tengo al menos una apuesta resuelta, cuando entro en mis estadísticas, entonces veo mi porcentaje de aciertos y mi rentabilidad acumulada.

## Social y notificaciones

**HU-36**: Como usuario, quiero ver un ranking de usuarios ordenado por ganancias o por porcentaje de aciertos, para poder compararme con mis compañeros. *(Fila 24)*
- Dado que hay usuarios con actividad registrada, cuando entro en el ranking, entonces veo una clasificación ordenada de todos ellos.
- Dado que estoy viendo el ranking, cuando cambio el criterio de ordenación (ganancias, aciertos, saldo), entonces la lista se reordena en consecuencia.
- Dado que se resuelve una apuesta, cuando vuelvo a consultar el ranking, entonces está actualizado.
- Dado que no estoy entre los primeros puestos, cuando consulto el ranking, entonces veo mi posición destacada igualmente.

**HU-37**: Como usuario, quiero recibir una notificación cuando se resuelve una de mis apuestas, para saber si he ganado o perdido sin tener que revisar la aplicación constantemente. *(Fila 23)*
- Dado que una apuesta cambia a estado resuelta, entonces recibo una notificación automática.
- Dado que recibo la notificación, entonces indica claramente el evento, el resultado y si gané o perdí.
- Dado que he descartado una notificación, cuando entro en mi historial de notificaciones, entonces puedo consultarla igualmente.

**HU-38**: Como usuario, quiero buscar y seguir a otro usuario desde su perfil, para poder ver después su actividad. *(Fila 26)*
- Dado que encuentro a otro usuario, cuando pulso "seguir" en su perfil, entonces empiezo a seguirle.

**HU-39**: Como usuario, quiero ver una lista de a quién sigo y quién me sigue, para gestionar mis conexiones dentro de la app. *(Fila 26)*
- Dado que sigo a otros usuarios, cuando entro en mi lista de conexiones, entonces veo a quién sigo y quién me sigue a mí.

**HU-40**: Como usuario, quiero consultar el historial de apuestas públicas y las estadísticas generales de los usuarios que sigo, para comparar mis resultados con los suyos. *(Fila 26)*
- Dado que sigo a un usuario, cuando entro en su perfil, entonces veo su historial de apuestas públicas y sus estadísticas generales, respetando el nivel de privacidad definido.

**HU-41**: Como usuario, quiero ver un feed de actividad con las últimas apuestas resueltas de los usuarios que sigo, para estar al tanto sin visitar cada perfil. *(Fila 26)*
- Dado que sigo a varios usuarios, cuando entro en la sección de actividad, entonces veo un feed con sus últimas apuestas resueltas.

**HU-42**: Como usuario, quiero elegir el nivel de privacidad de mi perfil (público, solo seguidores o privado), para decidir quién ve mis apuestas y mis estadísticas. *(Fila 26)*
- Dado que mi perfil es privado, cuando otro usuario entra en él, entonces no ve mi historial ni mis estadísticas.
- Dado que mi perfil es "solo seguidores", cuando alguien que no me sigue entra en él, entonces no ve mi historial.

**HU-43**: Como usuario, quiero copiar a mi boleto una apuesta pendiente de un usuario al que sigo, para apostar lo mismo que él. *(Fila 26)*
- Dado que veo una apuesta pendiente de un perfil al que tengo acceso, cuando pulso "copiar", entonces sus selecciones se añaden a mi boleto con las cuotas actuales.
- Dado que alguno de sus eventos ya ha empezado, entonces esa selección no se copia y se me avisa.

---

## Pendientes de refinar
Del bloque "Apostador" del PDF, sin criterios de aceptación:
- *Como apostador quiero poder introducir dinero para apostar.* Choca con "sin dinero real". Seguramente se refiere a la recarga de moneditas (HU-14).
- *Como apostador quiero poder consultar las apuestas para decidir en qué apostar.* Queda cubierta por HU-08, HU-19 y HU-20.
