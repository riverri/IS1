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
Columna **Puntos**: suma de los puntos de historia de las historias de cada fila ([historias](../requisitos/historias-de-usuario.md)); 391 en total: 186 de las filas hechas, 176 de las integradas (36 a 57 y 60) y 29 de las descartadas (filas 25 y 26); la 58 se descartó sin estimar. Con los 3 de HU-09 (requisito no funcional, sin fila propia; ver el punto 5), las filas hechas suman los 189 puntos de las historias hechas. De los 176 de las integradas, 3 son del requisito no funcional [RNF-01](../requisitos/requisitos-no-funcionales.md) (fila 41) y 173 de historias; las ampliaciones de las filas 33 y 42 (HU-102 a HU-113) están sin estimar. La columna **Estimación** es la estimación inicial, en fracciones de sprint.
Columna **Estado**: en qué iteración del [Sprint 1](sprints/sprint-1.md) se hizo (it. 1 = iteración 1). El detalle está en las actas de [sprints/iteraciones/](sprints/iteraciones/).

**Resumen:** 34 de 60 filas hechas (la 59, HU-54, se añadió el 27/11: estaba hecha desde el Sprint 3 sin fila propia); la 39 a la 41, la 43 a la 45 y la 47, integradas en `main` (falta que el Product Owner las acepte en la review del [Sprint 4](sprints/sprint-4.md)); la 36 a la 38, la 42, la 46 y la 48 a la 57, del [Sprint 5](sprints/sprint-5.md), también integradas (la 36 a la 38 y la 46, el 21/11); la 60 (HU-115 y HU-116), integrada el 22/11 y añadida al backlog el 27/11; la 58 se descartó el 27/11, y las 25 y 26 están descartadas (Won't).

| Orden | Elemento | Estimación | Puntos | Valor | MoSCoW | MVP | Estado |
|---:|---|---|---|---|:-:|:-:|---|
| 1 | Registro de competiciones, equipos y deportistas con su calificación de calidad (0–10), base para calcular los multiplicadores. Ej.: *Real Madrid (Fútbol; LaLiga, Champions; 9,5/10)*, *Lance Stroll (Automovilismo; F1; Aston Martin; 2/10)* | 1–2 sprints | 8 (HU-01) | 10 | M | ✅ | Hecho (it. 1) |
| 2 | Base de datos para almacenar equipos y competiciones. Actualización automática (API) o, si no es posible, manual | 2 sprints | 8 (HU-21) | 10 | M | ✅ | Hecho (it. 1 y 5) |
| 3 | Algoritmo de multiplicadores para apuestas básicas (G/E/P): a partir de la calificación, un factor manual de forma reciente y el volumen apostado en cada lado. *Intentar simplificarlo o dividirlo* | 1 sprint | 18 (HU-02, HU-03, HU-20) | 10 | M | ✅ | Hecho (it. 2 y 6) |
| 4 | Sistema de monedas virtuales para apostar y hacer recargas (sin dinero real) | ½ sprint | 4 (HU-13, HU-14) | 10 | M | ✅ | Hecho (it. 1) |
| 5 | Gestión de cuentas: registro, inicio de sesión, almacenamiento de emails y contraseñas | ½ sprint | 6 (HU-11, HU-12) | 10 | M | ✅ | Hecho (it. 1) |
| 6 | Gestionar la creación de apuestas del usuario | ½ sprint | 5 (HU-23) | 10 | M | ✅ | Hecho (it. 2) |
| 7 | Conectar la lógica con la interfaz de usuario | ½ sprint | — (sin historia propia) | 10 | M | ✅ | Hecho (it. 1–2) |
| 8 | Interfaz básica de usuario (inicio de sesión y landing page) | ½ sprint | — (sin historia propia) | 10 | M | ✅ | Hecho (it. 1) |
| 9 | Página con todas las apuestas (catálogo) | ¼ sprint | 5 (HU-08, HU-19) | 10 | M | ✅ | Hecho (it. 1) |
| 10 | Página de gestión de las apuestas del usuario | ¼ sprint | 3 (HU-24) | 10 | M | ✅ | Hecho (it. 2) |
| 11 | Página de resumen de apuestas | ¼ sprint | — (sin historia propia) | 10 | M | ✅ | Hecho (it. 3) |
| 12 | Página de saldo | ¼ sprint | — (sin historia propia) | 10 | M | ✅ | Hecho (it. 1) |
| 13 | Encontrar una API y enlazarla con la aplicación | 1 sprint | — (sin historia propia) | 7 (opcional) | C | | Hecho (it. 5 y 10) |
| 14 | Modificación de apuestas por parte del usuario | ½ sprint | 4 (HU-26, HU-27) | 5 (opcional) | C | | Hecho (it. 2 y 7) |
| 15 | Apuestas combinadas: boleto con selecciones de eventos distintos; multiplicador total = producto de cuotas; ganancia potencial = importe × multiplicador | ½ sprint | 10 (HU-28, HU-29) | 10 | M | ✅ | Hecho (it. 4) |
| 16 | Resolución automática de apuestas simples y combinadas al finalizar el evento y abono en el saldo | ½ sprint | 18 (HU-04, HU-25, HU-30) | 10 | M | ✅ | Hecho (it. 3) |
| 17 | Panel del creador de apuestas: alta y edición de eventos, resultados, suspensión/anulación y límites de apuesta | ½ sprint | 10 (HU-05, HU-06, HU-07) | 10 | M | ✅ | Hecho (it. 3, 4, 6 y 8) |
| 18 | Página del boleto (selecciones, importe, multiplicador, ganancia potencial) | ¼ sprint | — (HU-28 cuenta en la fila 15) | 10 | M | ✅ | Hecho (it. 4) |
| 19 | Historial de apuestas y estadísticas personales (% de aciertos, rentabilidad) | ½ sprint | 5 (HU-34, HU-35) | 8 | S | | Hecho (it. 3) |
| 20 | Banco de estadísticas de equipos: ficha, últimos resultados, evolución, cara a cara. *Depende de las filas 2 y 13* | 1 sprint | 11 (HU-31, HU-32, HU-33) | 8 | S | | Hecho (it. 7 y 9) |
| 21 | Búsqueda y filtros en el catálogo (deporte, competición, fecha, equipo) | ¼ sprint | 3 (HU-22) | 7 | S | | Hecho (it. 2) |
| 22 | Recuperación de contraseña y eliminación de cuenta | ¼ sprint | 8 (HU-17, HU-18) | 7 | S | | Hecho (it. 9; HU-17 en el Sprint 2) |
| 23 | Notificaciones al resolverse una apuesta | ½ sprint | 5 (HU-37) | 6 (opcional) | C | | Hecho (it. 7) |
| 24 | Ranking de usuarios (ganancias, aciertos, saldo) | ¼ sprint | 5 (HU-36) | 6 (opcional) | C | | Hecho (it. 2–3) |
| 25 | Vinculación de cuenta bancaria y retiradas de saldo | ½ sprint | 8 (HU-15, HU-16) | 4 (opcional) | W | | No se hará |
| 26 | Parte social: seguir usuarios, privacidad, feed, copiar apuestas | 1 sprint | 21 (HU-38 a HU-43) | 4 (opcional) | W | | No se hará (perfil público en la fila 31) |
| 27 | Límites de juego responsable y pausa temporal | ¼ sprint | 5 (HU-10) | 3 (opcional) | C | | Hecho (it. 9) |
| 28 | Apuestas a largo plazo: mercados con varios candidatos (Balón de Oro, campeón de LaLiga, Pichichi, campeón de F1). El creador crea el mercado, fija las cuotas de cada candidato, lo cierra y marca el ganador. *Depende de la fila 16* | ½–1 sprint | 10 (HU-44, HU-45) | 7 | S | | Hecho (it. 6) |
| 29 | Edición y borrado de eventos por el creador (HU-46) | ¼ sprint | 3 (HU-46) | 6 | S | | Hecho (it. 8) |
| 30 | Cambio de nombre y contraseña desde *Mi cuenta* (HU-47) | ¼ sprint | 2 (HU-47) | 5 | C | | Hecho (it. 8) |
| 31 | Perfil público de cada jugador desde el ranking, solo con sus estadísticas (HU-48). Versión mínima de la parte social | ¼ sprint | 2 (HU-48) | 5 | C | | Hecho (it. 8) |
| 32 | Plantillas de los equipos: jugadores con nota (0-10) y alineación probable, descargados de la API o gestionados a mano (HU-49, HU-50). *Depende de la fila 13* | ½ sprint | 10 (HU-49, HU-50) | 7 | S | | Hecho (it. 10) |
| 33 | Ligas privadas entre amigos con código de invitación y ranking propio (HU-51). Ampliación del Sprint 5: bloqueos entre amigos (HU-105 a HU-107), clasificación de una liga (HU-108 a HU-110, propuestas) y ligas públicas con su clasificación global (HU-111 a HU-113). *Depende de la fila 31* | ½ sprint | 5 (HU-51); la ampliación, sin estimar | 5 | C | | Hecho (it. 11); ampliación integrada en el Sprint 5 (HU-107 en parte; HU-108 a HU-110, propuestas) |
| 34 | Más tipos de apuesta en fútbol: doble oportunidad, más/menos 2,5 goles y ambos marcan (HU-52). *Depende de las filas 6 y 17* | ½ sprint | 8 (HU-52) | 7 | S | | Hecho (it. 11) |
| 35 | Panel de la casa para el creador: apostado, pagado, beneficio y margen real (HU-53) | ¼ sprint | 3 (HU-53) | 5 | S | | Hecho (it. 11) |
| 36 | Libro de movimientos del saldo: cada cambio del saldo queda apuntado con su motivo y el saldo resultante (HU-55). Base común del casino y de los mercados de predicción | ½ sprint | 5 (HU-55) | 6 | S | | Integrada (Sprint 5) |
| 37 | Casino con moneditas: ruleta europea, blackjack y baccarat, con el azar en el servidor, historial de partidas, límites por apuesta, cada movimiento en el libro del saldo y ventaja de la casa configurable con las reglas (HU-56 a HU-59 y HU-74). *Depende de la fila 36* | 1 sprint | 23 (HU-56 a HU-59 y HU-74) | 4 | C | | Integrada (Sprint 5) |
| 38 | Anuncios internos que promocionan otras secciones (largo plazo, ranking, ligas…): componente reutilizable, configuración desde Gestión, prioridad con rotación y registro de impresiones y clics (HU-60 a HU-62) | ½ sprint | 8 (HU-60 a HU-62) | 4 | C | | Integrada (Sprint 5) |
| 39 | Mercados de predicción de sí o no (tipo Polymarket) con precio de 0 a 1 calculado por un creador de mercado automático (LMSR): crear y cerrar, comprar y vender, resolver y pagar, estadísticas y posiciones de los compañeros de liga (HU-63 a HU-68). *Depende de las filas 33 y 36* | 1 sprint | 22 (HU-63 a HU-68) | 5 | C | | Integrada (06/11) |
| 40 | Cartera de criptomonedas reales a su precio real en euros (CoinGecko), compradas y vendidas con moneditas (1 monedita = 1 €), con las memecoins y las de alto riesgo como protagonistas: precios y gráfico, comprar y vender con comisión, mi cartera con ganancias y pérdidas, y configuración del creador (HU-69 a HU-73). *Depende de la fila 36* | 1 sprint | 18 (HU-69 a HU-73) | 4 | C | | Integrada (06/11) |
| 41 | Tiempo de carga mínimo: la web no se apaga por inactividad en Render (keep-alive: la aplicación se visita a sí misma cada 10 minutos), las páginas viajan comprimidas y la hoja de estilos se guarda en la caché del navegador. Es un requisito no funcional: [RNF-01](../requisitos/requisitos-no-funcionales.md) (antes HU-85, descartada como historia el 27/11) | ¼ sprint | 3 (RNF-01) | 7 | S | | Integrada (15/11) |
| 42 | Cobrar una apuesta antes de tiempo (cash-out), simple o combinada, sin apuestas en directo (HU-75). Ampliación: oferta, confirmación e historial del cash-out de una combinada entera (HU-102 a HU-104) | ½ sprint | 8 (HU-75); la ampliación, sin estimar | 5 | C | | Integrada (Sprint 5) |
| 43 | Riesgo de la casa por evento: lo que pagaría con cada resultado, aviso por encima de un límite y eventos marcados en el panel (HU-76) | ¼ sprint | 5 (HU-76) | 6 | S | | Integrada (16/11) |
| 44 | Correo al ganar una apuesta, con opción de desactivarlo en Mi cuenta (HU-77) | ¼ sprint | 3 (HU-77) | 4 | C | | Integrada (16/11) |
| 45 | Quiniela: montarla con 3 a 15 partidos, echar un boleto y repartir el bote entre los que más aciertan (HU-78 a HU-80) | ½ sprint | 11 (HU-78 a HU-80) | 4 | C | | Integrada (16/11) |
| 46 | Fantasy: equipo con jugadores reales, puntos por jornada y clasificación (HU-81 a HU-83) | ½ sprint | 12 (HU-81 a HU-83) | 3 | C | | Integrada (Sprint 5) |
| 47 | Ranking por tipo de juego: deportes, predicciones, cripto, memecoins y quiniela (HU-84). *Depende de las filas 39, 40 y 45* | ¼ sprint | 3 (HU-84) | 4 | C | | Integrada (16/11) |
| 48 | Seguimiento: equipos favoritos con sus partidos primero y aviso de los nuevos, alertas de cuota y evolución de la cuota de cada resultado (HU-86, HU-90 y HU-91) | ½ sprint | 9 (HU-86, HU-90 y HU-91) | 4 | C | | Integrada (Sprint 5) |
| 49 | Supercuota: cuota mejorada por el creador durante unas horas, con importe máximo y destacada en la portada (HU-87) | ¼ sprint | 3 (HU-87) | 5 | C | | Integrada (Sprint 5) |
| 50 | Logros en el perfil público (HU-88) | ¼ sprint | 5 (HU-88) | 3 | C | | Integrada (Sprint 5) |
| 51 | Reto 1 contra 1 entre compañeros de liga, sin comisión de la casa (HU-89). *Depende de la fila 33* | ¼ sprint | 5 (HU-89) | 4 | C | | Integrada (Sprint 5) |
| 52 | Resumen semanal por correo, desactivable en Mi cuenta (HU-92). *Depende de la fila 44* | ¼ sprint | 3 (HU-92) | 3 | C | | Integrada (Sprint 5) |
| 53 | Exportar mis apuestas y mis movimientos en CSV (HU-114) | ¼ sprint | 2 (HU-114) | 3 | C | | Integrada (Sprint 5) |
| 54 | Modo oscuro, recordado en una cookie (HU-94) | ¼ sprint | 2 (HU-94) | 3 | C | | Integrada (Sprint 5) |
| 55 | Beneficio de la casa en cada uno de los últimos 30 días (HU-95). *Depende de la fila 35* | ¼ sprint | 3 (HU-95) | 4 | C | | Integrada (Sprint 5) |
| 56 | Mis estadísticas por competición en Mis apuestas (HU-96) | ¼ sprint | 2 (HU-96) | 3 | C | | Integrada (Sprint 5) |
| 57 | Accesibilidad (nivel AA de WCAG 2.1): toda la web con el teclado y con lector de pantalla, buen contraste y ampliable, nada solo por el color y menos movimiento (HU-97 a HU-101) | ½ sprint | 15 (HU-97 a HU-101) | 4 | S | | Integrada (Sprint 5) |
| 58 | ~~Publicidad de Better en otras webs (HU-93)~~. Descartada el 27/11: no era una historia de usuario, y los anuncios dentro de la web ya son la fila 38 | — | — | — | W | | Descartada (27/11) |
| 59 | Filtro de nombres ofensivos: no se puede crear una cuenta ni cambiar el nombre a uno ofensivo, aunque se escriba con números, símbolos o tildes (HU-54) | ¼ sprint | 2 (HU-54) | 3 | C | | Hecho (Sprint 3) |
| 60 | Seguridad de la cuenta: recordar el navegador y cerrar esa sesión desde Mi cuenta, y cambiar el correo con confirmación en la dirección nueva (HU-115, HU-116). Añadida el 27/11: se integró el 22/11 sin fila | ¼ sprint | 6 (HU-115, HU-116) | 4 | S | | Integrada (Sprint 5) |

## Puntos a revisar en equipo

1. ~~**Estimación en puntos de historia.**~~ *Resuelto: la columna Puntos recoge los puntos de las historias de cada fila (escala Fibonacci). La columna Estimación queda como la estimación inicial.* Ahora está en fracciones de sprint. La teoría propone **puntos de historia** con escala Fibonacci, estimados con **Planning Poker** (cartas 0, 1, 2, 3, 5, 8, 13, 20, 40, 100). Conviene re-estimar al menos las historias del MVP en la primera Sprint Planning.
2. ~~**Tamaño del MVP.**~~ *Resuelto: el MVP quedó completo en el Sprint 1 (iteración 4).* Sumando las estimaciones de las filas del MVP salen unos **9–10 sprints** (las filas 1–3 suman 4–5). Hay que comprobarlo frente a los sprints que permita el cuatrimestre. Si no cabe, las filas 1–3 son las candidatas a dividirse, como ya indica el propio backlog.
3. ~~**"Dinero real".**~~ *Resuelto: las filas de dinero real son Won't y las moneditas son ficticias.* La fila 25 (cuenta bancaria y retiradas) y la historia "introducir dinero para apostar" chocan con la idea de **sin dinero real** de la propuesta enviada al profesor. Propuesta: que sean ficticias o marcarlas como **Won't**.
4. **Dependencias.** La fila 3 depende de la 1 y la 2. La 16 depende de la 6 y la 17. La 20 depende de la 2 y la 13. La 18 depende de la 15.
5. **Requisitos no funcionales.** "Web adaptable a móvil" (historia del bloque General) no tiene fila propia. Se puede añadir a la **Definición de Terminado** en lugar de al backlog.
