# Product Backlog

> Fuente: `Product_Backlog_IS1.pdf` del equipo. Lo mantiene el **Product Owner**, que es el único responsable de su contenido y su orden.
> El detalle de cada elemento (historias de usuario y criterios de aceptación) está en [../requisitos/historias-de-usuario.md](../requisitos/historias-de-usuario.md).
> Según la teoría (Scrum), cada elemento tiene **descripción, orden, estimación y valor**.

## MVP

> Un usuario puede registrarse, recibir sus moneditas, ver el catálogo con las cuotas calculadas por nuestro algoritmo, hacer apuestas simples y combinadas, y cobrar automáticamente cuando el creador de apuestas introduce el resultado del evento.

- **Incluye** las filas **1–12 y 15–18**, con equipos y resultados cargados a mano (sin API).
- **Queda fuera** del MVP: la API, las estadísticas de equipos y la parte social.

## Tabla

Columna **MoSCoW** (técnica de priorización del tema de estimación), deducida del MVP y del valor de cada fila.
**M** = Must · **S** = Should · **C** = Could · **W** = Won't (esta vez).
Columna **Estado**: en qué iteración del [Sprint 1](sprints/sprint-1.md) se hizo (it. 1 = iteración 1). El detalle está en las actas de [sprints/iteraciones/](sprints/iteraciones/).

**Resumen:** 33 de 38 filas hechas, de la 36 a la 38 en revisión y las 25 y 26 descartadas (Won't).

| Orden | Elemento | Estimación | Valor | MoSCoW | MVP | Estado |
|---:|---|---|---|:-:|:-:|---|
| 1 | Registro de competiciones, equipos y deportistas con su calificación de calidad (0–10), base para calcular los multiplicadores. Ej.: *Real Madrid (Fútbol; LaLiga, Champions; 9,5/10)*, *Lance Stroll (Automovilismo; F1; Aston Martin; 2/10)* | 1–2 sprints | 10 | M | ✅ | Hecho (it. 1) |
| 2 | Base de datos para almacenar equipos y competiciones. Actualización automática (API) o, si no es posible, manual | 2 sprints | 10 | M | ✅ | Hecho (it. 1 y 5) |
| 3 | Algoritmo de multiplicadores para apuestas básicas (G/E/P): a partir de la calificación, un factor manual de forma reciente y el volumen apostado en cada lado. *Intentar simplificarlo o dividirlo* | 1 sprint | 10 | M | ✅ | Hecho (it. 2 y 6) |
| 4 | Sistema de monedas virtuales para apostar y hacer recargas (sin dinero real) | ½ sprint | 10 | M | ✅ | Hecho (it. 1) |
| 5 | Gestión de cuentas: registro, inicio de sesión, almacenamiento de emails y contraseñas | ½ sprint | 10 | M | ✅ | Hecho (it. 1) |
| 6 | Gestionar la creación de apuestas del usuario | ½ sprint | 10 | M | ✅ | Hecho (it. 2) |
| 7 | Conectar la lógica con la interfaz de usuario | ½ sprint | 10 | M | ✅ | Hecho (it. 1–2) |
| 8 | Interfaz básica de usuario (inicio de sesión y landing page) | ½ sprint | 10 | M | ✅ | Hecho (it. 1) |
| 9 | Página con todas las apuestas (catálogo) | ¼ sprint | 10 | M | ✅ | Hecho (it. 1) |
| 10 | Página de gestión de las apuestas del usuario | ¼ sprint | 10 | M | ✅ | Hecho (it. 2) |
| 11 | Página de resumen de apuestas | ¼ sprint | 10 | M | ✅ | Hecho (it. 3) |
| 12 | Página de saldo | ¼ sprint | 10 | M | ✅ | Hecho (it. 1) |
| 13 | Encontrar una API y enlazarla con la aplicación | 1 sprint | 7 (opcional) | C | | Hecho (it. 5 y 10) |
| 14 | Modificación de apuestas por parte del usuario | ½ sprint | 5 (opcional) | C | | Hecho (it. 2 y 7) |
| 15 | Apuestas combinadas: boleto con selecciones de eventos distintos; multiplicador total = producto de cuotas; ganancia potencial = importe × multiplicador | ½ sprint | 10 | M | ✅ | Hecho (it. 4) |
| 16 | Resolución automática de apuestas simples y combinadas al finalizar el evento y abono en el saldo | ½ sprint | 10 | M | ✅ | Hecho (it. 3) |
| 17 | Panel del creador de apuestas: alta y edición de eventos, resultados, suspensión/anulación y límites de apuesta | ½ sprint | 10 | M | ✅ | Hecho (it. 3, 4, 6 y 8) |
| 18 | Página del boleto (selecciones, importe, multiplicador, ganancia potencial) | ¼ sprint | 10 | M | ✅ | Hecho (it. 4) |
| 19 | Historial de apuestas y estadísticas personales (% de aciertos, rentabilidad) | ½ sprint | 8 | S | | Hecho (it. 3) |
| 20 | Banco de estadísticas de equipos: ficha, últimos resultados, evolución, cara a cara. *Depende de las filas 2 y 13* | 1 sprint | 8 | S | | Hecho (it. 7 y 9) |
| 21 | Búsqueda y filtros en el catálogo (deporte, competición, fecha, equipo) | ¼ sprint | 7 | S | | Hecho (it. 2) |
| 22 | Recuperación de contraseña y eliminación de cuenta | ¼ sprint | 7 | S | | Hecho (it. 9; HU-17 en el Sprint 2) |
| 23 | Notificaciones al resolverse una apuesta | ½ sprint | 6 (opcional) | C | | Hecho (it. 7) |
| 24 | Ranking de usuarios (ganancias, aciertos, saldo) | ¼ sprint | 6 (opcional) | C | | Hecho (it. 2–3) |
| 25 | Vinculación de cuenta bancaria y retiradas de saldo | ½ sprint | 4 (opcional) | W | | No se hará |
| 26 | Parte social: seguir usuarios, privacidad, feed, copiar apuestas | 1 sprint | 4 (opcional) | W | | No se hará (perfil público en la fila 31) |
| 27 | Límites de juego responsable y pausa temporal | ¼ sprint | 3 (opcional) | C | | Hecho (it. 9) |
| 28 | Apuestas a largo plazo: mercados con varios candidatos (Balón de Oro, campeón de LaLiga, Pichichi, campeón de F1). El creador crea el mercado, fija las cuotas de cada candidato, lo cierra y marca el ganador. *Depende de la fila 16* | ½–1 sprint | 7 | S | | Hecho (it. 6) |
| 29 | Edición y borrado de eventos por el creador (HU-46) | ¼ sprint | 6 | S | | Hecho (it. 8) |
| 30 | Cambio de nombre y contraseña desde *Mi cuenta* (HU-47) | ¼ sprint | 5 | C | | Hecho (it. 8) |
| 31 | Perfil público de cada jugador desde el ranking, solo con sus estadísticas (HU-48). Versión mínima de la parte social | ¼ sprint | 5 | C | | Hecho (it. 8) |
| 32 | Plantillas de los equipos: jugadores con nota (0-10) y alineación probable, descargados de la API o gestionados a mano (HU-49, HU-50). *Depende de la fila 13* | ½ sprint | 7 | S | | Hecho (it. 10) |
| 33 | Ligas privadas entre amigos con código de invitación y ranking propio (HU-51). *Depende de la fila 31* | ½ sprint | 5 | C | | Hecho (it. 11) |
| 34 | Más tipos de apuesta en fútbol: doble oportunidad, más/menos 2,5 goles y ambos marcan (HU-52). *Depende de las filas 6 y 17* | ½ sprint | 7 | S | | Hecho (it. 11) |
| 35 | Panel de la casa para el creador: apostado, pagado, beneficio y margen real (HU-54) | ¼ sprint | 5 | S | | Hecho (it. 11) |
| 36 | Libro de movimientos del saldo: cada cambio del saldo queda apuntado con su motivo y el saldo resultante (HU-55). Base común del casino y de los mercados de predicción | ½ sprint | 6 | S | | En revisión (PR) |
| 37 | Casino con moneditas: ruleta europea y blackjack, con el azar en el servidor, historial de partidas, límites por apuesta, cada movimiento en el libro del saldo y ventaja de la casa configurable con las reglas (HU-56 a HU-59). *Depende de la fila 36* | 1 sprint | 4 | C | | En revisión (PR) |
| 38 | Anuncios internos que promocionan otras secciones (largo plazo, ranking, ligas…): componente reutilizable, configuración desde Gestión, prioridad con rotación y registro de impresiones y clics (HU-60 a HU-62) | ½ sprint | 4 | C | | En revisión (PR) |

## Puntos a revisar en equipo

1. **Estimación en puntos de historia.** Ahora está en fracciones de sprint. La teoría propone **puntos de historia** con escala Fibonacci, estimados con **Planning Poker** (cartas 0, 1, 2, 3, 5, 8, 13, 20, 40, 100). Conviene re-estimar al menos las historias del MVP en la primera Sprint Planning.
2. ~~**Tamaño del MVP.**~~ *Resuelto: el MVP quedó completo en el Sprint 1 (iteración 4).* Sumando las estimaciones de las filas del MVP salen unos **9–10 sprints** (las filas 1–3 suman 4–5). Hay que comprobarlo frente a los sprints que permita el cuatrimestre. Si no cabe, las filas 1–3 son las candidatas a dividirse, como ya indica el propio backlog.
3. ~~**"Dinero real".**~~ *Resuelto: las filas de dinero real son Won't y las moneditas son ficticias.* La fila 25 (cuenta bancaria y retiradas) y la historia "introducir dinero para apostar" chocan con la idea de **sin dinero real** de la propuesta enviada al profesor. Propuesta: que sean ficticias o marcarlas como **Won't**.
4. **Dependencias.** La fila 3 depende de la 1 y la 2. La 16 depende de la 6 y la 17. La 20 depende de la 2 y la 13. La 18 depende de la 15.
5. **Requisitos no funcionales.** "Web adaptable a móvil" (historia del bloque General) no tiene fila propia. Se puede añadir a la **Definición de Terminado** en lugar de al backlog.
