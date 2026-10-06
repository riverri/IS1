# Historias de usuario

> Fuente: `Product_Backlog_IS1.pdf`. Formato de la asignatura: *Como [actor], quiero [acción] para [objetivo]*, con criterios de aceptación *Dado… cuando… entonces…*.
> Cada criterio tiene un identificador **CA-xx.y** (historia xx, criterio y). La [matriz de trazabilidad](trazabilidad.md) usa los mismos identificadores para enlazar cada criterio con su prueba.
> La columna **Fila** remite al [Product Backlog](../scrum/product-backlog.md).

## Cómo leer cada historia

- **Historia:** quién la pide, qué quiere y para qué.
- **Ficha:** fila del Product Backlog, prioridad MoSCoW de esa fila, sprint en que se hizo y estado.
- **Criterios de aceptación:** las condiciones que se tienen que cumplir para dar la historia por terminada (Definition of Done). Cada uno se lee como *Dado* (situación de partida), *Cuando* (lo que hace el usuario o el sistema) y *Entonces* (el resultado que se espera). 

## Actores

- **Visitante**: no ha iniciado sesión.
- **Usuario / apostador**: registrado; apuesta con moneditas virtuales.
- **Creador de apuestas** (administrador): gestiona equipos, eventos, cuotas, resultados y límites.

## Resumen

| Historia | Título | Estado | Criterios |
|---|---|---|---:|
| HU-01 | Alta de competiciones y equipos con su calificación | Hecha | 2 |
| HU-02 | Forma reciente de un equipo | Hecha | 1 |
| HU-03 | Cuotas automáticas con margen para la casa | Hecha | 3 |
| HU-04 | Introducir el resultado | Hecha | 2 |
| HU-05 | Suspender o anular un evento | Hecha | 2 |
| HU-06 | Dinero apostado a cada resultado | Hecha | 1 |
| HU-07 | Límites de apuesta del creador | Hecha | 1 |
| HU-08 | Catálogo sin registrarse | Hecha | 2 |
| HU-09 | Web adaptada a móvil | Hecha | 1 |
| HU-10 | Juego responsable | Hecha | 2 |
| HU-11 | Registro | Hecha | 2 |
| HU-12 | Inicio de sesión | Hecha | 2 |
| HU-13 | Consultar el saldo | Hecha | 2 |
| HU-14 | Saldo de bienvenida y recargas | Hecha | 2 |
| HU-15 | Cuenta bancaria | Descartada (Won't) | 2 |
| HU-16 | Retirar el saldo | Descartada (Won't) | 3 |
| HU-17 | Recuperar la contraseña | Pendiente (issue #27) | 2 |
| HU-18 | Eliminar la cuenta | Hecha | 2 |
| HU-19 | Catálogo por deporte y fecha | Hecha | 2 |
| HU-20 | Cuotas de cada resultado | Hecha | 2 |
| HU-21 | Cargar eventos a mano o desde la API | Hecha | 2 |
| HU-22 | Búsqueda y filtros | Hecha | 3 |
| HU-23 | Apuesta simple | Hecha | 2 |
| HU-24 | Apuestas activas | Hecha | 1 |
| HU-25 | Resolución automática | Hecha | 3 |
| HU-26 | Cancelar una apuesta | Hecha | 2 |
| HU-27 | Cambiar el importe | Hecha | 2 |
| HU-28 | Boleto de combinadas | Hecha | 3 |
| HU-29 | Multiplicador total y ganancia potencial | Hecha | 3 |
| HU-30 | Resolución de combinadas | Hecha | 3 |
| HU-31 | Ficha del equipo | Hecha | 2 |
| HU-32 | Gráfico de evolución | Hecha | 1 |
| HU-33 | Cara a cara | Hecha | 2 |
| HU-34 | Historial | Hecha | 1 |
| HU-35 | Estadísticas personales | Hecha | 1 |
| HU-36 | Ranking | Hecha | 4 |
| HU-37 | Avisos | Hecha | 3 |
| HU-38 | Seguir a otro usuario | Descartada (Won't) | 1 |
| HU-39 | Seguidos y seguidores | Descartada (Won't) | 1 |
| HU-40 | Actividad de los usuarios que sigo | Descartada (Won't) | 1 |
| HU-41 | Feed de actividad | Descartada (Won't) | 1 |
| HU-42 | Privacidad del perfil | Descartada (Won't) | 2 |
| HU-43 | Copiar una apuesta | Descartada (Won't) | 2 |
| HU-44 | Apuestas a largo plazo | Hecha | 3 |
| HU-45 | Gestión de mercados | Hecha | 3 |
| HU-46 | Editar o borrar un evento | Hecha | 2 |
| HU-47 | Cambiar nombre y contraseña | Hecha | 2 |
| HU-48 | Perfil público de un jugador | Hecha | 1 |
| HU-49 | Plantilla y alineación probable | Hecha | 2 |
| HU-50 | Plantillas desde la API y a mano | Hecha | 2 |
| HU-51 | Ligas privadas | Hecha | 3 |
| HU-52 | Doble oportunidad, goles y ambos marcan | Hecha | 3 |
| HU-54 | Cuentas de la casa | Hecha | 2 |

**53 historias**: 44 hechas, 1 pendiente y 8 descartadas. **107 criterios de aceptación** en total; 92 de ellos son de historias hechas.

---

## Creador de apuestas

### HU-01 · Alta de competiciones y equipos con su calificación

**Como** creador de apuestas, **quiero** dar de alta competiciones, equipos y deportistas con su calificación de calidad (0–10), **para** que el sistema tenga la base con la que calcular las cuotas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 1, 2 | Must | 1 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-01.1 | Doy de alta un equipo con su deporte, sus competiciones y su calificación | Lo guardo | Queda disponible para crear eventos con él. |
| CA-01.2 | Introduzco una calificación fuera del rango 0–10 | Intento guardarla | El sistema la rechaza. |

### HU-02 · Forma reciente de un equipo

**Como** creador de apuestas, **quiero** ajustar manualmente el factor de forma reciente de un equipo (rachas, moral, lesiones…), **para** que las cuotas reflejen su momento actual y no solo su calidad general.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 3 | Must | 6 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-02.1 | Modifico el factor de forma de un equipo | Lo guardo | Se recalculan las cuotas de sus eventos que aún no han empezado. |

### HU-03 · Cuotas automáticas con margen para la casa

**Como** creador de apuestas, **quiero** que las cuotas iniciales (G/E/P) de cada evento se generen automáticamente con un margen para la casa, **para** no tener que fijarlas a mano.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 3 | Must | 2, 6 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-03.1 | Creo un evento entre dos equipos registrados | Lo guardo | El sistema propone una cuota para cada resultado a partir de su calidad y su forma. |
| CA-03.2 | El sistema genera o recalcula las cuotas de un evento | Se suman las probabilidades implícitas (1/cuota) | La suma de las probabilidades implícitas (1/cuota) es siempre mayor que 1, para que la casa no pierda a largo plazo. |
| CA-03.3 | Se ha apostado mucho a un resultado | Su cuota baja por el volumen apostado | Esta nunca baja de un mínimo fijado (p. ej. 1,01). |

### HU-04 · Introducir el resultado

**Como** creador de apuestas, **quiero** introducir el resultado final de un evento, **para** que se resuelvan todas las apuestas asociadas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 16, 17 | Must | 3 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-04.1 | Un evento ha terminado | Introduzco su resultado y lo confirmo | El evento pasa a "finalizado" y se lanza la resolución de sus apuestas. |
| CA-04.2 | Me he equivocado al introducir el resultado | Lo corrijo | Las apuestas se vuelven a resolver y los saldos se ajustan. |

### HU-05 · Suspender o anular un evento

**Como** creador de apuestas, **quiero** suspender o anular un evento (aplazamiento, error en las cuotas…), **para** que nadie apueste en condiciones incorrectas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 17 | Must | 3, 5 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-05.1 | Un evento programado | Lo suspendo | Deja de admitir apuestas nuevas hasta que lo reactive. |
| CA-05.2 | Un evento con apuestas | Lo anulo | Se devuelve a cada usuario el importe que apostó en él. |

### HU-06 · Dinero apostado a cada resultado

**Como** creador de apuestas, **quiero** ver cuánto se ha apostado en cada resultado de un evento, **para** comprobar que el ajuste de cuotas funciona y detectar desequilibrios.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 17 | Must | 3 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-06.1 | Un evento con apuestas | Entro en él desde el panel de gestión | Veo el importe total y el número de apuestas de cada resultado. |

### HU-07 · Límites de apuesta del creador

**Como** creador de apuestas, **quiero** fijar límites de apuesta (importe mínimo y máximo, número máximo de selecciones en una combinada), **para** controlar el riesgo de la casa.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 17 | Must | 4, 6 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-07.1 | Un usuario intenta apostar fuera de esos límites | Confirma la apuesta | El sistema lo impide y le indica el límite. |

### HU-46 · Editar o borrar un evento

**Como** creador de apuestas, **quiero** editar o borrar un evento que he creado mal (fecha, equipos, fase), **para** corregir errores sin tener que anularlo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 29 | Should | 8 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-46.1 | Un evento aún no ha empezado | Cambio su fecha, sus equipos o su fase | Se guardan los cambios y las cuotas se recalculan. |
| CA-46.2 | Un evento no tiene apuestas | Lo borro | Desaparece del catálogo. Si tiene apuestas, solo se puede anular (se devuelve el importe). |

### HU-54 · Cuentas de la casa

**Como** creador de apuestas, **quiero** ver un panel con lo que se ha apostado, lo que se ha pagado y el beneficio de la casa, **para** comprobar si las cuotas están bien ajustadas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 35 | Should | 11 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-54.1 | Hay apuestas resueltas | Entro en el panel de la casa | Veo el total apostado, el total pagado, el beneficio y el margen real frente al margen teórico. |
| CA-54.2 | Hay apuestas de varios tipos y deportes | Entro en el panel de la casa | Veo el desglose por tipo de apuesta (simples, combinadas y largo plazo) y por deporte. |

---

## General

### HU-08 · Catálogo sin registrarse

**Como** visitante, **quiero** consultar el catálogo de eventos y sus cuotas sin estar registrado, **para** decidir si me interesa crear una cuenta.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 9 | Must | 1 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-08.1 | No he iniciado sesión | Entro en la web | Puedo ver los eventos y sus cuotas. |
| CA-08.2 | No he iniciado sesión | Intento apostar | Se me redirige al inicio de sesión o al registro. |

### HU-09 · Web adaptada a móvil

**Como** usuario, **quiero** que la web se adapte a móvil y a ordenador, **para** poder apostar desde cualquier dispositivo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| Requisito no funcional | — | 9 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-09.1 | Accedo desde un móvil | Navego por la web | Todas las páginas se ven y se usan correctamente. |

### HU-10 · Juego responsable

**Como** usuario, **quiero** poder fijarme límites de apuesta (diarios o semanales) o una pausa temporal, **para** jugar de forma responsable.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 27 | Could | 9 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-10.1 | He fijado un límite diario | Lo alcanzo | No puedo apostar más hasta el día siguiente. |
| CA-10.2 | He activado una pausa | Intento apostar antes de que termine | No puedo apostar hasta que termine, aunque sí puedo consultar mi cuenta. |

---

## Gestión de usuarios

### HU-11 · Registro

**Como** usuario, **quiero** registrarme y crear un perfil, **para** poder acceder a la aplicación con una cuenta propia.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 5 | Must | 1 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-11.1 | Relleno el formulario de registro con un email y una contraseña válidos | Lo envío | Se crea mi cuenta y mi perfil. |
| CA-11.2 | Intento registrarme con un email ya existente | Lo envío | El sistema me avisa de que ya hay una cuenta con ese email. |

### HU-12 · Inicio de sesión

**Como** usuario, **quiero** iniciar sesión de forma segura, **para** proteger el acceso a mi cuenta y a mi saldo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 5, 8 | Must | 1 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-12.1 | Introduzco mi email y contraseña correctos | Inicio sesión | Accedo a mi cuenta. |
| CA-12.2 | Los datos de acceso son incorrectos | Inicio sesión | El sistema deniega el acceso con un mensaje de error genérico. |

### HU-13 · Consultar el saldo

**Como** usuario, **quiero** consultar mi saldo de moneditas virtuales en cualquier momento, **para** tener control de cuánto tengo disponible para apostar.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 4, 12 | Must | 1 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-13.1 | Soy un usuario registrado | Entro en mi cuenta | Veo mi saldo actual de moneditas. |
| CA-13.2 | Mi saldo cambia (apuesta o recarga) | Lo vuelvo a consultar | El valor está actualizado. |

### HU-14 · Saldo de bienvenida y recargas

**Como** usuario, **quiero** recibir una recarga inicial de moneditas al registrarme (y recargas periódicas gratuitas), **para** poder seguir jugando sin aportar dinero real.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 4 | Must | 1 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-14.1 | Soy un usuario nuevo | Completo el registro | Se me asigna automáticamente el saldo de bienvenida. |
| CA-14.2 | Ha pasado el periodo definido desde mi última recarga gratuita | Inicio sesión o abro mi cuenta | Recibo automáticamente una nueva recarga. |

### HU-15 · Cuenta bancaria

**Como** usuario, **quiero** añadir y gestionar los datos de una cuenta bancaria (real o ficticia), **para** poder retirar mi saldo cuando lo desee.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 25 | Won't | — | Descartada (Won't) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-15.1 | No tengo ninguna cuenta bancaria vinculada | Añado una | Queda guardada asociada a mi perfil. |
| CA-15.2 | Ya tengo una cuenta vinculada | La edito o la sustituyo por otra | Se actualiza la que se usará para futuras retiradas. |

### HU-16 · Retirar el saldo

**Como** usuario, **quiero** retirar mi saldo disponible a mi cuenta bancaria vinculada, **para** poder disponer de mis ganancias fuera de la aplicación.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 25 | Won't | — | Descartada (Won't) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-16.1 | Tengo saldo disponible (no comprometido en apuestas activas) y una cuenta vinculada | Solicito una retirada por un importe igual o menor a ese saldo | Se descuenta de mi saldo y la solicitud queda registrada. |
| CA-16.2 | Solicito retirar más saldo del disponible | Confirmo la operación | El sistema la rechaza. |
| CA-16.3 | Se completa una retirada | Consulto mi historial | Aparece registrada con su importe y fecha. |

### HU-17 · Recuperar la contraseña

**Como** usuario, **quiero** recuperar el acceso si olvido mi contraseña, **para** no perder mi cuenta ni mi saldo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 22 | Should | — | Pendiente (issue #27) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-17.1 | Pulso "he olvidado mi contraseña" e introduzco mi email | Lo envío | Recibo un enlace para crear una contraseña nueva. |
| CA-17.2 | El enlace ha caducado o ya se ha usado | Lo abro | El sistema no permite cambiar la contraseña. |

### HU-18 · Eliminar la cuenta

**Como** usuario, **quiero** eliminar mi cuenta, **para** que se borren mis datos personales si dejo de usar la aplicación.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 22 | Should | 9 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-18.1 | Tengo apuestas activas | Intento eliminar la cuenta | El sistema me avisa de que las perderé y me pide confirmación. |
| CA-18.2 | He pedido eliminar mi cuenta | Confirmo la eliminación | Ya no puedo iniciar sesión y mis datos personales se borran. |

### HU-47 · Cambiar nombre y contraseña

**Como** usuario, **quiero** cambiar mi nombre y mi contraseña desde *Mi cuenta*, **para** mantener mis datos al día.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 30 | Could | 8 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-47.1 | Introduzco mi contraseña actual y una nueva válida | La guardo | La siguiente vez entro con la nueva. |
| CA-47.2 | La contraseña actual no es correcta | Intento cambiarla | El sistema no lo permite. |

---

## Catálogo de eventos deportivos

### HU-19 · Catálogo por deporte y fecha

**Como** usuario, **quiero** ver una lista de eventos deportivos disponibles organizados por deporte y fecha, **para** poder apostar a tiempo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 9 | Must | 1 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-19.1 | Hay eventos disponibles | Entro en el catálogo | Los veo agrupados por deporte y ordenados por fecha. |
| CA-19.2 | Un evento ya ha empezado o terminado | Consulto el catálogo | Ya no aparece como disponible para apostar. |

### HU-20 · Cuotas de cada resultado

**Como** usuario, **quiero** ver las cuotas asociadas a cada resultado posible de un evento, **para** apostar cuando el riesgo-recompensa compense.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 3, 9 | Must | 2 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-20.1 | Entro en un evento | Lo consulto | Veo una cuota para cada resultado posible (G/E/P). |
| CA-20.2 | Las cuotas cambian por el volumen apostado | Vuelvo a consultar el evento | Veo las cuotas actualizadas. |

### HU-21 · Cargar eventos a mano o desde la API

**Como** administrador, **quiero** cargar o actualizar eventos y cuotas manualmente o desde una fuente externa, **para** corregir errores y mantener los datos al día.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 2, 13, 17 | Could, Must | 5 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-21.1 | Doy de alta o edito un evento | Lo guardo | Se refleja de inmediato en el catálogo que ve el usuario. |
| CA-21.2 | La fuente externa está disponible | Se sincroniza | Los eventos y cuotas se actualizan sin intervención manual. *(fuera del MVP)*. |

### HU-22 · Búsqueda y filtros

**Como** usuario, **quiero** buscar un equipo o una competición y filtrar el catálogo, **para** encontrar rápido el evento que me interesa.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 21 | Should | 2 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-22.1 | Escribo el nombre de un equipo | Busco | Veo solo sus eventos disponibles. |
| CA-22.2 | Estoy en el catálogo | Aplico filtros (deporte, competición, fecha) | El catálogo muestra solo los eventos que los cumplen. |
| CA-22.3 | Ningún evento cumple la búsqueda o los filtros | Busco o filtro | Se muestra un mensaje que lo indica. |

---

## Apuesta simple

### HU-23 · Apuesta simple

**Como** usuario, **quiero** seleccionar un evento y una cuota y confirmar una apuesta con parte de mi saldo, **para** intentar ganar más moneditas si acierto el resultado.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 6 | Must | 2 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-23.1 | Tengo saldo suficiente | Confirmo una apuesta | El importe se descuenta de mi saldo y la apuesta queda registrada como activa. |
| CA-23.2 | No tengo saldo suficiente | Intento confirmar la apuesta | El sistema me lo impide y me avisa. |

### HU-24 · Apuestas activas

**Como** usuario, **quiero** ver el estado de mis apuestas activas, **para** saber en todo momento cuánto tengo comprometido y en qué apuestas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 10, 11 | Must | 2 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-24.1 | Tengo apuestas activas | Entro en mi sección de apuestas | Veo cada una con su evento, cuota e importe. |

### HU-25 · Resolución automática

**Como** usuario, **quiero** que mis apuestas se resuelvan automáticamente en cuanto termina el evento, **para** recibir mis ganancias (o ver la pérdida) sin tener que reclamarlo yo mismo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 16 | Must | 3, 5 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-25.1 | Un evento tiene apuestas activas | Pasa a "finalizado" con un resultado | Todas sus apuestas asociadas pasan a "ganada" o "perdida" según corresponda. |
| CA-25.2 | Tengo una apuesta activa | Pasa a "ganada" | Mi saldo se actualiza automáticamente con la ganancia. |
| CA-25.3 | Se resuelve una apuesta | Consulto mi historial | Veo el resultado sin haber hecho nada manualmente. |

### HU-26 · Cancelar una apuesta

**Como** usuario, **quiero** cancelar una apuesta activa antes de que empiece el evento, **para** recuperar el importe si cambio de opinión.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 14 | Could | 2 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-26.1 | El evento aún no ha empezado | Cancelo la apuesta | Se me devuelve el importe al saldo. |
| CA-26.2 | El evento ya ha empezado | Intento cancelarla | El sistema no lo permite. |

### HU-27 · Cambiar el importe

**Como** usuario, **quiero** modificar el importe de una apuesta activa antes de que empiece el evento, **para** ajustar lo que arriesgo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 14 | Could | 7 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-27.1 | Subo el importe y tengo saldo suficiente | Confirmo | Se descuenta la diferencia y se aplica la cuota vigente en ese momento. |
| CA-27.2 | Bajo el importe | Confirmo | Se me devuelve la diferencia al saldo. |

### HU-52 · Doble oportunidad, goles y ambos marcan

**Como** usuario, **quiero** apostar a más cosas de un partido de fútbol (doble oportunidad, más o menos de 2,5 goles y ambos marcan), **para** tener más opciones que el 1X2.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 34 | Should | 11 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-52.1 | Hay un partido de fútbol programado | Entro en su página | Veo las cuotas de doble oportunidad (1X, X2, 12), de más/menos de 2,5 goles y de ambos marcan (sí/no). |
| CA-52.2 | Estoy en un partido de fútbol | Añado una de estas selecciones al boleto | Puedo apostarla sola o en una combinada. |
| CA-52.3 | Hay apuestas de estos tipos en un partido | El creador introduce el marcador final | Estas selecciones se resuelven solas; si solo se conoce el ganador, las de goles se anulan (cuota 1,00). |

---

## Apuestas combinadas

### HU-28 · Boleto de combinadas

**Como** usuario, **quiero** añadir a un mismo boleto selecciones de varios eventos, **para** crear mi propia apuesta combinada.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 15, 18 | Must | 4 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-28.1 | Tengo selecciones en el boleto | Añado otra de un evento distinto | Se añade al boleto. |
| CA-28.2 | Intento añadir dos selecciones del mismo evento | Las añado | El sistema no lo permite y me avisa. |
| CA-28.3 | Tengo selecciones en el boleto | Quito una de ellas | Desaparece y, si solo queda una, el boleto pasa a ser una apuesta simple. |

### HU-29 · Multiplicador total y ganancia potencial

**Como** usuario, **quiero** que el sistema calcule el multiplicador total del boleto y la ganancia potencial, **para** saber cuánto puedo ganar antes de confirmar.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 15, 18 | Must | 4 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-29.1 | El boleto tiene varias selecciones | Lo abro | El multiplicador total es el producto de las cuotas de todas ellas. |
| CA-29.2 | Tengo un boleto con selecciones | Introduzco un importe | Veo la ganancia potencial (importe × multiplicador total) antes de confirmar. |
| CA-29.3 | Tengo un boleto con selecciones | Cambia la cuota de alguna antes de confirmar | El multiplicador se recalcula y se me pide que acepte la nueva cuota. |

### HU-30 · Resolución de combinadas

**Como** usuario, **quiero** que mi combinada se resuelva según el resultado de todas sus selecciones, **para** cobrar solo si acierto todas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 16 | Must | 4 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-30.1 | Tengo una combinada activa | Falla una de sus selecciones | La combinada pasa a "perdida" sin esperar a que terminen los demás eventos. |
| CA-30.2 | Tengo una combinada activa | Aciertan todas sus selecciones | La combinada pasa a "ganada" y se me abona la ganancia. |
| CA-30.3 | Tengo una combinada activa | Uno de sus eventos se anula | Esa selección cuenta con cuota 1,00 y el resto de la combinada sigue en juego. |

---

## Banco de estadísticas de equipos

### HU-31 · Ficha del equipo

**Como** usuario, **quiero** consultar la ficha de un equipo o deportista con sus últimos resultados y su posición en la clasificación, **para** valorar su forma antes de apostar.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 20 | Should | 7 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-31.1 | Un equipo con partidos jugados | Entro en su ficha | Veo sus últimos 5 resultados y su posición en cada competición. |
| CA-31.2 | Estoy viendo un evento | Pulso sobre uno de los equipos | Accedo a su ficha. |

### HU-32 · Gráfico de evolución

**Como** usuario, **quiero** ver la evolución de un equipo a lo largo de la temporada en un gráfico, **para** detectar tendencias (rachas, mejora o bajón).

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 20 | Should | 9 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-32.1 | Entro en la ficha de un equipo | Abro la pestaña de evolución | Veo un gráfico con sus puntos o su posición jornada a jornada. |

### HU-49 · Plantilla y alineación probable

**Como** usuario, **quiero** ver la plantilla de cada equipo con la nota de cada jugador y su alineación probable, **para** saber con quién juega antes de apostar.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 32 | Should | 10 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-49.1 | Un equipo de fútbol con jugadores suficientes | Entro en su ficha | Veo la alineación probable (4-3-3) en un campo, con los jugadores de mejor nota en cada posición. |
| CA-49.2 | Un equipo con jugadores | Entro en su ficha | Veo la plantilla agrupada por posición, con dorsal, nacionalidad, edad y nota. |

### HU-50 · Plantillas desde la API y a mano

**Como** creador de apuestas, **quiero** descargar las plantillas reales desde la API y añadir, puntuar o quitar jugadores a mano, **para** mantenerlas al día.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 32 | Should | 10 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-50.1 | La API está configurada | Pulso *Descargar plantillas* | Se crean o actualizan los jugadores de nuestros equipos sin cambiar las notas que ya puse. |
| CA-50.2 | Un jugador ha dejado el equipo en la API | Se descargan las plantillas | Desaparece de la plantilla; los jugadores añadidos a mano no se tocan. |

### HU-33 · Cara a cara

**Como** usuario, **quiero** comparar cara a cara a los dos rivales de un evento, **para** decidir mi apuesta con más información.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 20 | Should | 7 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-33.1 | Entro en un evento | Pulso "comparar" | Veo el historial de enfrentamientos entre ambos y sus estadísticas principales una junto a otra. |
| CA-33.2 | Dos equipos sin enfrentamientos previos | Entro en el partido entre ellos | Se indica y se muestran solo las estadísticas de cada uno. |

---

## Historial y estadísticas personales

### HU-34 · Historial

**Como** usuario, **quiero** ver mi historial completo de apuestas con sus resultados, **para** poder repasar mis decisiones pasadas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 19 | Should | 3 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-34.1 | Tengo apuestas resueltas | Entro en mi historial | Veo cada una con su evento, cuota, importe y resultado. |

### HU-35 · Estadísticas personales

**Como** usuario, **quiero** ver estadísticas básicas de mi actividad (porcentaje de aciertos, rentabilidad), **para** saber si mi balance global es positivo o negativo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 19 | Should | 3 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-35.1 | Tengo al menos una apuesta resuelta | Entro en mis estadísticas | Veo mi porcentaje de aciertos y mi rentabilidad acumulada. |

---

## Social y notificaciones

### HU-36 · Ranking

**Como** usuario, **quiero** ver un ranking de usuarios ordenado por ganancias o por porcentaje de aciertos, **para** poder compararme con mis compañeros.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 24 | Could | 2, 3 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-36.1 | Hay usuarios con actividad registrada | Entro en el ranking | Veo una clasificación ordenada de todos ellos. |
| CA-36.2 | Estoy viendo el ranking | Cambio el criterio de ordenación (ganancias, aciertos, saldo) | La lista se reordena en consecuencia. |
| CA-36.3 | Se resuelve una apuesta | Vuelvo a consultar el ranking | Está actualizado. |
| CA-36.4 | No estoy entre los primeros puestos | Consulto el ranking | Veo mi posición destacada igualmente. |

### HU-37 · Avisos

**Como** usuario, **quiero** recibir una notificación cuando se resuelve una de mis apuestas, **para** saber si he ganado o perdido sin tener que revisar la aplicación constantemente.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 23 | Could | 7 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-37.1 | Tengo una apuesta activa | Se resuelve | Recibo una notificación automática. |
| CA-37.2 | Una de mis apuestas se ha resuelto | Abro la notificación | Indica claramente el evento, el resultado y si gané o perdí. |
| CA-37.3 | He descartado una notificación | Entro en mi historial de notificaciones | Puedo consultarla igualmente. |

### HU-38 · Seguir a otro usuario

**Como** usuario, **quiero** buscar y seguir a otro usuario desde su perfil, **para** poder ver después su actividad.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 26 | Won't | — | Descartada (Won't) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-38.1 | Encuentro a otro usuario | Pulso "seguir" en su perfil | Empiezo a seguirle. |

### HU-39 · Seguidos y seguidores

**Como** usuario, **quiero** ver una lista de a quién sigo y quién me sigue, **para** gestionar mis conexiones dentro de la app.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 26 | Won't | — | Descartada (Won't) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-39.1 | Sigo a otros usuarios | Entro en mi lista de conexiones | Veo a quién sigo y quién me sigue a mí. |

### HU-40 · Actividad de los usuarios que sigo

**Como** usuario, **quiero** consultar el historial de apuestas públicas y las estadísticas generales de los usuarios que sigo, **para** comparar mis resultados con los suyos.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 26 | Won't | — | Descartada (Won't) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-40.1 | Sigo a un usuario | Entro en su perfil | Veo su historial de apuestas públicas y sus estadísticas generales, respetando el nivel de privacidad definido. |

### HU-41 · Feed de actividad

**Como** usuario, **quiero** ver un feed de actividad con las últimas apuestas resueltas de los usuarios que sigo, **para** estar al tanto sin visitar cada perfil.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 26 | Won't | — | Descartada (Won't) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-41.1 | Sigo a varios usuarios | Entro en la sección de actividad | Veo un feed con sus últimas apuestas resueltas. |

### HU-42 · Privacidad del perfil

**Como** usuario, **quiero** elegir el nivel de privacidad de mi perfil (público, solo seguidores o privado), **para** decidir quién ve mis apuestas y mis estadísticas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 26 | Won't | — | Descartada (Won't) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-42.1 | Mi perfil es privado | Otro usuario entra en él | No ve mi historial ni mis estadísticas. |
| CA-42.2 | Mi perfil es "solo seguidores" | Alguien que no me sigue entra en él | No ve mi historial. |

### HU-43 · Copiar una apuesta

**Como** usuario, **quiero** copiar a mi boleto una apuesta pendiente de un usuario al que sigo, **para** apostar lo mismo que él.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 26 | Won't | — | Descartada (Won't) |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-43.1 | Veo una apuesta pendiente de un perfil al que tengo acceso | Pulso "copiar" | Sus selecciones se añaden a mi boleto con las cuotas actuales. |
| CA-43.2 | Alguno de los eventos de la apuesta que quiero copiar ya ha empezado | La copio a mi boleto | Esa selección no se copia y se me avisa. |

### HU-48 · Perfil público de un jugador

**Como** usuario, **quiero** ver el perfil público de otro jugador desde el ranking, con su porcentaje de aciertos y su balance, **para** compararme con él sin tener que seguirlo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 31 | Could | 8 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-48.1 | Pulso un nombre en el ranking | Se abre su perfil | Veo sus estadísticas pero no sus apuestas. |

### HU-51 · Ligas privadas

**Como** usuario, **quiero** crear una liga privada con mis amigos y unirme con un código de invitación, **para** competir en un ranking solo entre nosotros.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 33 | Could | 11 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-51.1 | Soy un jugador | Creo una liga | Se genera un código que puedo compartir y yo quedo como miembro. |
| CA-51.2 | Tengo el código de una liga | Lo introduzco | Entro en ella y veo su ranking con los mismos criterios que el ranking general. |
| CA-51.3 | Soy miembro | Salgo de la liga | Dejo de aparecer en su ranking; si soy quien la creó, puedo borrarla. |

---

## Apuestas a largo plazo

### HU-44 · Apuestas a largo plazo

**Como** usuario, **quiero** apostar a quién ganará un premio o una competición (Balón de Oro, campeón de LaLiga, Pichichi…), **para** predecir resultados a largo plazo.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 28 | Should | 6 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-44.1 | Hay un mercado abierto | Entro en él | Veo todos los candidatos con su cuota. |
| CA-44.2 | Elijo un candidato y un importe con saldo suficiente | Confirmo | Se descuenta el importe y la apuesta queda activa. |
| CA-44.3 | El mercado está cerrado | Intento apostar | El sistema no lo permite. |

### HU-45 · Gestión de mercados

**Como** creador de apuestas, **quiero** crear un mercado a largo plazo con sus candidatos y cuotas, cerrarlo y marcar el ganador, **para** gestionar este tipo de apuestas.

| Fila del backlog | Prioridad | Sprint | Estado |
|---|---|---|---|
| 28 | Should | 6 | Hecha |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-45.1 | Creo un mercado con al menos dos candidatos y sus cuotas | Lo guardo | Aparece en el catálogo. |
| CA-45.2 | Un mercado abierto | Lo cierro | Deja de admitir apuestas. |
| CA-45.3 | Un mercado con apuestas | Marco el candidato ganador | Las apuestas a ese candidato se cobran y el resto pasan a perdidas. |

---

## Pendientes de refinar

Del bloque "Apostador" del PDF, sin criterios de aceptación:
- *Como apostador quiero poder introducir dinero para apostar.* Choca con "sin dinero real". Seguramente se refiere a la recarga de moneditas (HU-14).
- *Como apostador quiero poder consultar las apuestas para decidir en qué apostar.* Queda cubierta por HU-08, HU-19 y HU-20.
