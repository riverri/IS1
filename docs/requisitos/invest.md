# Criterio INVEST de las historias de usuario

Las historias de [historias-de-usuario.md](historias-de-usuario.md) siguen el criterio **INVEST**: cada historia tiene que ser **I**ndependiente, **N**egociable, **V**aliosa, **E**stimable, pequeña (**S**mall) y comprobable (**T**estable). Este documento explica qué se ha pedido a cada historia para cumplir cada letra y lo comprueba historia por historia, también las que no lo cumplen del todo.

## Qué significa cada letra y cómo se cumple

| Letra | Qué pide | Cómo se cumple en Better | Cómo se ha comprobado |
|---|---|---|---|
| **I** · Independiente | Se puede hacer, probar y aceptar sin esperar a otra historia pendiente | Cada historia es una funcionalidad completa que se ve en la web. Las que amplían otra (p. ej. cambiar el importe, HU-27, sobre la apuesta simple, HU-23) se planificaron cuando la base ya estaba hecha | Leyendo las historias que citan a otras. Se marcan con ⚠ las que van detrás de otra del mismo grupo o se solapan con una existente |
| **N** · Negociable | Dice *qué* se quiere, no *cómo* hacerlo; el detalle se acuerda con el Product Owner | Los criterios describen lo que ve o recibe cada actor; la solución técnica va aparte, en el código y en la [diseño](../diseno/). Las historias se han renegociado cuando hizo falta: por ejemplo, HU-93 perdió un criterio que ya cubría HU-61 (#137), y el 27/11 HU-85 y HU-93 se descartaron porque no eran historias de usuario | Revisión del texto de cada historia |
| **V** · Valiosa | Aporta algo a un actor concreto | Formato *Como [actor], quiero [acción], para [objetivo]*; el actor es uno de los [actores](historias-de-usuario.md#actores) (visitante, usuario o creador) y el *para* dice el beneficio | Automática: rol reconocido como actor y cláusula *para* presente |
| **E** · Estimable | Se puede estimar su tamaño | Cada historia tiene puntos, líneas, horas y coste de IA ([Estimación](historias-de-usuario.md#estimación)) | Automática: tiene puntos en la tabla de resumen |
| **S** · Pequeña | Cabe en un sprint | Hasta 8 puntos | Automática: puntos ≤ 8. Con ⚠ las de más puntos |
| **T** · Comprobable | Se puede comprobar si está hecha | Criterios de aceptación *Dado… cuando… entonces…* con identificador CA-xx.y, enlazados con su prueba en la [matriz de trazabilidad](trazabilidad.md); además, la [Definition of Done](../scrum/proceso.md#definition-of-done-una-historia-está-terminada-si) común | Automática: tiene al menos un criterio de aceptación |

## Resultado

Hay 114 historias: los números HU-85 y HU-93 son huecos de dos historias descartadas el 27/11 porque no lo eran (lo que pedía HU-85, ya hecho, es ahora el requisito no funcional [RNF-01](requisitos-no-funcionales.md)). **101 de las 114** cumplen las seis letras sin reservas. El resto:

- **I (independiente), con reservas:** HU-103 y HU-104, HU-107 a HU-111. Son historias de un mismo grupo que se hacen juntas (cash-out de combinadas, bloqueos y clasificación de ligas) o que se solapan con el ranking de cada liga (HU-51).
- **E (estimable), sin estimar todavía:** HU-102 a HU-113. Están bien definidas y se pueden estimar; falta poner sus puntos. Por eso tampoco se puede comprobar la S.
- **S (pequeña), con reservas:** HU-03.
- **V (valiosa) y T (comprobable):** todas las historias cumplen.

Las descartadas (Won't) también se escribieron con este criterio; se incluyen para que la tabla esté completa.

## Historia por historia

✓ cumple · ⚠ cumple con reservas (ver nota) · ✗ no cumple · — no se puede comprobar. En **T**, entre paréntesis, el número de criterios de aceptación.

| Historia | Título | Estado | Actor | I | N | V | E | S | T | Notas |
|---|---|---|---|:-:|:-:|:-:|:-:|:-:|:-:|---|
| HU-01 | Alta de competiciones y equipos con su calificación | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-02 | Forma reciente de un equipo | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-03 | Cuotas automáticas con margen para la casa | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ⚠ | ✓ (3) | S: la más grande (13 puntos); se hizo en dos partes, en las iteraciones 2 y 6 del Sprint 1. |
| HU-04 | Introducir el resultado | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-05 | Suspender o anular un evento | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-06 | Dinero apostado a cada resultado | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-07 | Límites de apuesta del creador | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-08 | Catálogo sin registrarse | Hecha | Visitante | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-09 | Web adaptada a móvil | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-10 | Juego responsable | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-11 | Registro | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (4) |  |
| HU-12 | Inicio de sesión | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-13 | Consultar el saldo | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-14 | Saldo de bienvenida y recargas | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-15 | Cuenta bancaria | Descartada (Won't) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-16 | Retirar el saldo | Descartada (Won't) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-17 | Recuperar la contraseña | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-18 | Eliminar la cuenta | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-19 | Catálogo por deporte y fecha | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-20 | Cuotas de cada resultado | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-21 | Cargar eventos a mano o desde la API | Hecha | Creador (administrador) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-22 | Búsqueda y filtros | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-23 | Apuesta simple | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-24 | Apuestas activas | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-25 | Resolución automática | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-26 | Cancelar una apuesta | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-27 | Cambiar el importe | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-28 | Boleto de combinadas | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-29 | Multiplicador total y ganancia potencial | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-30 | Resolución de combinadas | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-31 | Ficha del equipo | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-32 | Gráfico de evolución | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-33 | Cara a cara | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-34 | Historial | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-35 | Estadísticas personales | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-36 | Ranking | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (4) |  |
| HU-37 | Avisos | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-38 | Seguir a otro usuario | Descartada (Won't) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-39 | Seguidos y seguidores | Descartada (Won't) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-40 | Actividad de los usuarios que sigo | Descartada (Won't) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-41 | Feed de actividad | Descartada (Won't) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-42 | Privacidad del perfil | Descartada (Won't) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-43 | Copiar una apuesta | Descartada (Won't) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-44 | Apuestas a largo plazo | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-45 | Gestión de mercados | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-46 | Editar o borrar un evento | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-47 | Cambiar nombre y contraseña | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-48 | Perfil público de un jugador | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-49 | Plantilla y alineación probable | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-50 | Plantillas desde la API y a mano | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-51 | Ligas privadas | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-52 | Doble oportunidad, goles y ambos marcan | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-53 | Cuentas de la casa | Hecha | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-54 | Nombres ofensivos | Hecha | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-55 | Movimientos del saldo | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-56 | Ruleta europea | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-57 | Blackjack | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (5) |  |
| HU-58 | Historial de partidas del casino | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-59 | Configurar el casino | Integrada (Sprint 5) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-60 | Anuncios de otras secciones | Integrada (Sprint 5) | Visitante o usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-61 | Gestionar los anuncios | Integrada (Sprint 5) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-62 | Medir los clics de cada anuncio | Integrada (Sprint 5) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-63 | Crear, cerrar y anular mercados de predicción | Integrada (06/11) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-64 | Comprar participaciones | Integrada (06/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-65 | Vender participaciones | Integrada (06/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-66 | Resolver el mercado y pagar | Integrada (06/11) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-67 | Estadísticas y evolución del precio | Integrada (06/11) | Visitante | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-68 | Posiciones de mis compañeros de liga | Integrada (06/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-69 | Ver criptomonedas a su precio real | Integrada (06/11) | Visitante | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (4) |  |
| HU-70 | Comprar criptomonedas con moneditas | Integrada (06/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (4) |  |
| HU-71 | Vender criptomonedas | Integrada (06/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-72 | Mi cartera de criptomonedas | Integrada (06/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-73 | Configurar las criptomonedas | Integrada (06/11) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-74 | Baccarat | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (4) | I: se apoya en el casino (HU-56 a HU-59), que ya estaba hecho al planificarla. |
| HU-75 | Cobrar antes de tiempo (cash-out) | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (4) |  |
| HU-76 | Riesgo de la casa por evento | Integrada (16/11) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-77 | Correo al ganar una apuesta | Integrada (16/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-78 | Montar una quiniela | Integrada (16/11) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-79 | Echar un boleto | Integrada (16/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-80 | Repartir el bote de la quiniela | Integrada (16/11) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-81 | Crear un equipo fantasy y fichar | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-82 | Puntos de los jugadores fantasy | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-83 | Clasificación fantasy | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (1) |  |
| HU-84 | Ranking por tipo de juego | Integrada (16/11) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) | I: filtra por los juegos de otras historias (casino, predicciones, cripto y quiniela), ya hechas al planificarla. |
| HU-85 | Hueco: no era una historia de usuario | Descartada (27/11) | — | — | — | — | — | — | — | Se mantiene el número para no cambiar el orden. Lo que pedía es ahora [RNF-01](requisitos-no-funcionales.md). |
| HU-86 | Equipos favoritos | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-87 | Supercuota | Integrada (Sprint 5) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-88 | Logros | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-89 | Reto 1 contra 1 | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-90 | Alerta de cuota | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-91 | Evolución de la cuota | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-92 | Resumen semanal por correo | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-93 | Hueco: no era una historia de usuario | Descartada (27/11) | — | — | — | — | — | — | — | Se mantiene el número para no cambiar el orden. |
| HU-94 | Modo oscuro | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-95 | Beneficio de la casa por día | Integrada (Sprint 5) | Creador | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-96 | Estadísticas por competición | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-97 | Usar la web solo con el teclado | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-98 | Usar la web con un lector de pantalla | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (4) |  |
| HU-99 | Leer bien con poca visión | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-100 | No depender solo del color | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (3) |  |
| HU-101 | Menos movimiento | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-102 | Ver la oferta de cash-out de una combinada | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✗ | — | ✓ (4) | E: falta estimarla. |
| HU-103 | Confirmar el cash-out de toda la combinada | Integrada (Sprint 5) | Usuario | ⚠ | ✓ | ✓ | ✗ | — | ✓ (4) | I: va después de HU-102 (confirma la oferta que muestra esa historia). E: falta estimarla. |
| HU-104 | Consultar el cobro anticipado en el historial | Integrada (Sprint 5) | Usuario | ⚠ | ✓ | ✓ | ✗ | — | ✓ (3) | I: va después de HU-103 (enseña el cash-out ya cobrado). E: falta estimarla. |
| HU-105 | Bloquear a un amigo en un partido o selección | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✗ | — | ✓ (5) | E: falta estimarla. |
| HU-106 | Bloquear a toda la liga en un partido o selección | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✗ | — | ✓ (4) | E: falta estimarla. |
| HU-107 | Aplicar y caducar los bloqueos de la liga | Integrada en parte (Sprint 5) | Usuario | ⚠ | ✓ | ✓ | ✗ | — | ✓ (5) | I: aplica los bloqueos que crean HU-105 y HU-106; se hace con ellas. E: falta estimarla. |
| HU-108 | Consultar la clasificación de una liga (propuesta) | Propuesta | Usuario | ⚠ | ✓ | ✓ | ✗ | — | ✓ (3) | I: se solapa con el ranking de cada liga (HU-51), que ya da casi todo lo que pide. E: falta estimarla. |
| HU-109 | Ordenar la clasificación y consultar mi puesto (propuesta) | Propuesta | Usuario | ⚠ | ✓ | ✓ | ✗ | — | ✓ (4) | I: se solapa con el ranking de cada liga (HU-51). E: falta estimarla. |
| HU-110 | Mantener la clasificación limitada a los miembros (propuesta) | Propuesta | Usuario | ⚠ | ✓ | ✓ | ✗ | — | ✓ (3) | I: se solapa con el ranking de cada liga (HU-51). E: falta estimarla. |
| HU-111 | Alternar entre mis ligas y la clasificación global | Integrada (Sprint 5) | Usuario | ⚠ | ✓ | ✓ | ✗ | — | ✓ (4) | I: necesita la clasificación global de HU-112. E: falta estimarla. |
| HU-112 | Consultar la clasificación global de ligas públicas | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✗ | — | ✓ (4) | E: falta estimarla. |
| HU-113 | Cambiar la visibilidad de una liga | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✗ | — | ✓ (5) | E: falta estimarla. |
| HU-114 | Exportar mi historial | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (2) |  |
| HU-115 | Recordar este navegador | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (4) |  |
| HU-116 | Cambiar el correo de mi cuenta | Integrada (Sprint 5) | Usuario | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ (5) |  |

*Tabla generada a partir de [historias-de-usuario.md](historias-de-usuario.md) el 27/11/2026: si cambia una historia, hay que revisar su fila.*
