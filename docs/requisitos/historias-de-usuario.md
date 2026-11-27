# Historias de usuario

> Fuente: `Product_Backlog_IS1.pdf`. Formato de la asignatura: *Como [actor], quiero [acción] para [objetivo]*, con criterios de aceptación *Dado… cuando… entonces…*.
> Cada criterio tiene un identificador **CA-xx.y** (historia xx, criterio y). La [matriz de trazabilidad](trazabilidad.md) usa los mismos identificadores para enlazar cada criterio con su prueba.
> La columna **Fila** remite al [Product Backlog](../scrum/product-backlog.md).

## Cómo leer cada historia

- **Historia:** quién la pide, qué quiere y para qué.
- **Ficha:** fila del Product Backlog, prioridad del 1 (muy alta) al 5 (muy baja), coherente con el MoSCoW de su fila (M → 1–2, S → 2–3, C → 3–4, W → 5), sprint en que se hizo (en el Sprint 1, también la iteración: «1 (it. 3)»), estado y estimación: puntos de historia, líneas de código, horas sin IA y con IA y coste de la IA (ver [Estimación](#estimación)).
- **Criterios de aceptación:** las condiciones propias de esta historia que se tienen que cumplir para aceptarla. Además, toda historia cumple la [Definition of Done](../scrum/proceso.md#definition-of-done-una-historia-está-terminada-si) común a todas. Cada criterio se lee como *Dado* (situación de partida), *Cuando* (lo que hace el usuario o el sistema) y *Entonces* (el resultado que se espera). 

Cómo cumple cada historia los criterios INVEST (independiente, negociable, valiosa, estimable, pequeña y comprobable): [invest.md](invest.md).

## Estimación

Cada historia tiene un **tamaño** (puntos y líneas de código), las **horas** que llevaría sin IA y con IA, y el **coste de la IA** (tokens y dinero). Lo que se puede medir en el repositorio se ha medido; lo que no, sale de valores de referencia publicados y se da con un intervalo.

### 1. Tamaño

- **Puntos de historia** (Fibonacci): tamaño relativo, decidido al planificar cada iteración. En las historias hechas son los de las actas de las iteraciones; si se hizo en dos iteraciones, se suman (HU-03: 8 + 3). En las descartadas, una estimación por comparación con historias parecidas.
- **Líneas de código**: se han contado las líneas reales del repositorio, sin comentarios ni líneas en blanco: 15.103 en total (Java de la aplicación, pruebas, vistas HTML, CSS y JavaScript y migraciones SQL). Repartidas entre los 185 puntos hechos, salen **82 líneas por punto**. Las líneas de cada historia son sus puntos × 82.

### 2. Horas sin IA

Horas de una persona del equipo que programa la historia sin IA: diseñarla, escribir el código y las pruebas, depurarla y dejarla revisada. Dependen de la **productividad** (líneas terminadas por hora), que no se puede medir en este proyecto porque se hizo con IA. Se toman valores de referencia y se combinan con la fórmula de **tres valores (PERT)**: (optimista + 4 × probable + pesimista) / 6.

| | Optimista | Probable | Pesimista | Resultado |
|---|---:|---:|---:|---:|
| Líneas por hora | 25 | 15 | 8 | 13,9 líneas/h |

- **Optimista, 25:** el orden de magnitud que se mide en los cursos del Personal Software Process con programas pequeños e individuales.
- **Probable, 15:** en un proyecto en equipo con un framework que se está aprendiendo (Spring Boot), con pruebas automáticas y coordinación, se produce menos.
- **Pesimista, 8:** cuando hay que aprender la tecnología sobre la marcha.

Con 13,9 líneas por hora, **un punto (82 líneas) son unas 5,9 horas sin IA**.

*Comprobación:* el modelo COCOMO básico (proyecto pequeño, equipo con experiencia en el dominio) da para 15,1 mil líneas unos 42 meses-persona. Eso incluye gestión y documentación de un proyecto profesional, así que es un techo, no la estimación.

### 3. Horas con IA

Con IA la persona no escribe el código, pero tiene que dirigir a la IA y responder de lo que entrega. Se separa en cuatro partes:

| Parte | Cómo se calcula | En qué se basa |
|---|---|---|
| **Dirigir a la IA** | 0,95 h por cada mil líneas | **Medido:** el código de las 12 iteraciones del Sprint 1 (13.625 líneas) se generó en unas 13 horas de reloj de trabajo con la IA |
| **Revisar el código** | Leerlo y entenderlo a 138 líneas/h | Tres valores: 300, 150 y 75 líneas/h. Los estudios de revisión de código recomiendan no pasar de 200–400 líneas/h para encontrar fallos; quien aprende Spring necesita ir más despacio |
| **Comprobar los criterios** | 10 minutos por criterio de aceptación | Probar a mano en el navegador el *Dado / Cuando / Entonces* de cada criterio de la historia |
| **Corregir fallos** | 2,5 fallos por cada mil líneas × 1 h | **Medido:** la revisión encontró 34 fallos en 13.625 líneas (issues #34–#67). Cada uno: entenderlo, corregirlo con ayuda y comprobarlo |

Para un punto (82 líneas): 0,1 h de dirigir + 0,6 h de revisar + 0,2 h de corregir, más los criterios de la historia. **La parte que más pesa es revisar**: con IA, el trabajo pasa de escribir código a leerlo y entenderlo.

### 4. Coste de la IA

Se ha medido con el registro de la sesión de trabajo con la IA, que guarda los tokens de cada respuesta, y las tarifas de la API de Anthropic del modelo usado (octubre de 2026): 4 $ por millón de tokens de entrada, 20 $ por millón de salida, 0,20 $ por millón leídos de la caché y 5–8 $ por millón escritos en ella.

| Fase | Tokens generados | Tokens de contexto leídos | Coste |
|---|---:|---:|---:|
| Programar la app (Sprint 1, 13.625 líneas) | 1,0 millones | 372 millones | 114 $ |
| Revisar y corregir los 34 fallos | 0,3 millones | 107 millones | 47 $ |
| **Por cada mil líneas** | **92 mil** | **35 millones** | **11,8 $** |

- **Por punto (82 líneas)** salen unos 1,0 $.
- **Casi todo el coste es contexto:** en cada paso la IA vuelve a leer la conversación y los archivos (la caché lo abarata). Lo que escribe es una parte pequeña.
- **Son precios de la API.** Con una suscripción mensual a Claude se paga una cuota fija, no por token.
- **No incluye** el coste de los documentos, las presentaciones ni la wiki, que no son código.

### Totales

| | Puntos | Líneas | Horas sin IA | Horas con IA | Tokens generados | Coste de la IA |
|---|---:|---:|---:|---:|---:|---:|
| **Historias hechas (46)** | 185 | 15.170 | 1.091 | 178 | 1,4 millones | 179 $ |
| Historias integradas (56; sin estimar 9 de ellas: HU-102 a HU-107 y HU-111 a HU-113) | 170 | 13.940 | 1.005 | 164 | 1,3 millones | 164 $ |
| Historias por hacer (1: HU-93, sin estimar) | — | — | — | — | — | — |
| Historias descartadas (8) | 29 | 2.378 | 171 | 28 | 0,2 millones | 28 $ |

**Cómo se reparten las 178 horas con IA de las historias hechas:** 14 h dirigiendo a la IA, 110 h revisando el código, 16 h comprobando criterios y 38 h corrigiendo fallos.

**Lectura:**
- **Sin IA,** las historias hechas suman unas 1.091 horas, unas 218 por persona. Con lo que cinco estudiantes dedican a una asignatura en un cuatrimestre no cabe.
- **Con IA,** bajan a unas 178 horas, unas 36 por persona, y el coste es de unos 179 $.
- **El ahorro solo es real si la revisión se hace.** Si se salta, el tiempo baja todavía más, pero nadie del equipo conoce el código, y eso es lo que se evalúa en la defensa.

**Límites de la estimación:**
- **Líneas de cada historia:** salen de sus puntos; las que realmente ocupa cada una pueden variar.
- **Fallos:** solo se cuentan los que encontró la revisión.
- **Productividad sin IA y ritmo de revisión:** son valores de referencia, no medidos aquí.
- **Para afinarla:** conviene apuntar en cada acta las horas reales de revisión y compararlas con estas.

## Actores

- **Visitante**: no ha iniciado sesión.
- **Usuario / apostador**: registrado; apuesta con moneditas virtuales.
- **Creador de apuestas** (administrador): gestiona equipos, eventos, cuotas, resultados y límites.

## Resumen

| Historia | Título | Estado | Criterios | Prioridad | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|---:|
| HU-01 | Alta de competiciones y equipos con su calificación | Hecha | 2 | 1 (Muy alta) | 8 | 656 | 47 | 7,3 | 7,8 $ |
| HU-02 | Forma reciente de un equipo | Hecha | 1 | 2 (Alta) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-03 | Cuotas automáticas con margen para la casa | Hecha | 3 | 2 (Alta) | 13 | 902 | 65 | 10 | 10,7 $ |
| HU-04 | Introducir el resultado | Hecha | 2 | 1 (Muy alta) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-05 | Suspender o anular un evento | Hecha | 2 | 2 (Alta) | 5 | 328 | 24 | 3,8 | 3,9 $ |
| HU-06 | Dinero apostado a cada resultado | Hecha | 1 | 2 (Alta) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-07 | Límites de apuesta del creador | Hecha | 1 | 2 (Alta) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-08 | Catálogo sin registrarse | Hecha | 2 | 1 (Muy alta) | 2 | 164 | 12 | 2,1 | 1,9 $ |
| HU-09 | Web adaptada a móvil | Hecha | 2 | 5 (Muy baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-10 | Juego responsable | Hecha | 2 | 4 (Baja) | 5 | 492 | 35 | 5,6 | 5,8 $ |
| HU-11 | Registro | Hecha | 3 | 1 (Muy alta) | 3 | 246 | 18 | 3,1 | 2,9 $ |
| HU-12 | Inicio de sesión | Hecha | 2 | 1 (Muy alta) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-13 | Consultar el saldo | Hecha | 2 | 1 (Muy alta) | 2 | 164 | 12 | 2,1 | 1,9 $ |
| HU-14 | Saldo de bienvenida y recargas | Hecha | 2 | 2 (Alta) | 2 | 164 | 12 | 2,1 | 1,9 $ |
| HU-15 | Cuenta bancaria | Descartada (Won't) | 2 | 5 (Muy baja) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-16 | Retirar el saldo | Descartada (Won't) | 3 | 5 (Muy baja) | 3 | 246 | 18 | 3,1 | 2,9 $ |
| HU-17 | Recuperar la contraseña | Hecha | 2 | 3 (Media) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-18 | Eliminar la cuenta | Hecha | 2 | 2 (Alta) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-19 | Catálogo por deporte y fecha | Hecha | 2 | 1 (Muy alta) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-20 | Cuotas de cada resultado | Hecha | 2 | 2 (Alta) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-21 | Cargar eventos a mano o desde la API | Hecha | 2 | 2 (Alta) | 8 | 656 | 47 | 7,3 | 7,8 $ |
| HU-22 | Búsqueda y filtros | Hecha | 3 | 3 (Media) | 3 | 246 | 18 | 3,1 | 2,9 $ |
| HU-23 | Apuesta simple | Hecha | 2 | 1 (Muy alta) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-24 | Apuestas activas | Hecha | 1 | 2 (Alta) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-25 | Resolución automática | Hecha | 3 | 2 (Alta) | 8 | 656 | 47 | 7,5 | 7,8 $ |
| HU-26 | Cancelar una apuesta | Hecha | 2 | 2 (Alta) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-27 | Cambiar el importe | Hecha | 2 | 2 (Alta) | 2 | 164 | 12 | 2,1 | 1,9 $ |
| HU-28 | Boleto de combinadas | Hecha | 3 | 2 (Alta) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-29 | Multiplicador total y ganancia potencial | Hecha | 3 | 2 (Alta) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-30 | Resolución de combinadas | Hecha | 3 | 2 (Alta) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-31 | Ficha del equipo | Hecha | 2 | 3 (Media) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-32 | Gráfico de evolución | Hecha | 1 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-33 | Cara a cara | Hecha | 2 | 3 (Media) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-34 | Historial | Hecha | 1 | 2 (Alta) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-35 | Estadísticas personales | Hecha | 1 | 2 (Alta) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-36 | Ranking | Hecha | 4 | 2 (Alta) | 5 | 410 | 29 | 5,0 | 4,8 $ |
| HU-37 | Avisos | Hecha | 3 | 4 (Baja) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-38 | Seguir a otro usuario | Descartada (Won't) | 1 | 5 (Muy baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-39 | Seguidos y seguidores | Descartada (Won't) | 1 | 5 (Muy baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-40 | Actividad de los usuarios que sigo | Descartada (Won't) | 1 | 5 (Muy baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-41 | Feed de actividad | Descartada (Won't) | 1 | 5 (Muy baja) | 5 | 410 | 29 | 4,5 | 4,8 $ |
| HU-42 | Privacidad del perfil | Descartada (Won't) | 2 | 5 (Muy baja) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-43 | Copiar una apuesta | Descartada (Won't) | 2 | 5 (Muy baja) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-44 | Apuestas a largo plazo | Hecha | 3 | 3 (Media) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-45 | Gestión de mercados | Hecha | 3 | 3 (Media) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-46 | Editar o borrar un evento | Hecha | 2 | 1 (Muy alta) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-47 | Cambiar nombre y contraseña | Hecha | 3 | 3 (Media) | 2 | 164 | 12 | 2,2 | 1,9 $ |
| HU-48 | Perfil público de un jugador | Hecha | 1 | 3 (Media) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-49 | Plantilla y alineación probable | Hecha | 2 | 4 (Baja) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-50 | Plantillas desde la API y a mano | Hecha | 2 | 3 (Media) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-51 | Ligas privadas | Hecha | 3 | 4 (Baja) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-52 | Doble oportunidad, goles y ambos marcan | Hecha | 3 | 3 (Media) | 8 | 656 | 47 | 7,5 | 7,8 $ |
| HU-53 | Cuentas de la casa | Hecha | 2 | 3 (Media) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-54 | Nombres inapropiados | Hecha| 1 | 4 (Baja) | 2 | 153 | 12 | 1,8 | 1,7 $ |
| HU-55 | Movimientos del saldo | Integrada (Sprint 5) | 3 | 3 (Media) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-56 | Ruleta europea | Integrada (Sprint 5) | 3 | 4 (Baja) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-57 | Blackjack | Integrada (Sprint 5) | 5 | 4 (Baja) | 8 | 656 | 47 | 7,6 | 7,8 $ |
| HU-58 | Historial de partidas del casino | Integrada (Sprint 5) | 1 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-59 | Configurar el casino | Integrada (Sprint 5) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-60 | Anuncios de otras secciones | Integrada (Sprint 5) | 3 | 4 (Baja) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-61 | Gestionar los anuncios | Integrada (Sprint 5) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-62 | Medir los clics de cada anuncio | Integrada (Sprint 5) | 1 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-63 | Crear, cerrar y anular mercados de predicción | Integrada (06/11) | 3 | 4 (Baja) | 3 | 246 | 18 | 3,0 | 2,9 $ |
| HU-64 | Comprar participaciones | Integrada (06/11) | 3 | 4 (Baja) | 8 | 656 | 47 | 7,5 | 7,8 $ |
| HU-65 | Vender participaciones | Integrada (06/11) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-66 | Resolver el mercado y pagar | Integrada (06/11) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-67 | Estadísticas y evolución del precio | Integrada (06/11) | 1 | 4 (Baja) | 3 | 246 | 18 | 2,7 | 2,9 $ |
| HU-68 | Posiciones de mis compañeros de liga | Integrada (06/11) | 2 | 4 (Baja) | 2 | 164 | 12 | 2,1 | 1,9 $ |
| HU-69 | Ver criptomonedas a su precio real | Integrada (06/11) | 4 | 4 (Baja) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-70 | Comprar criptomonedas con moneditas | Integrada (06/11) | 4 | 4 (Baja) | 5 | 410 | 29 | 4,9 | 4,8 $ |
| HU-71 | Vender criptomonedas | Integrada (06/11) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,8 | 2,9 $ |
| HU-72 | Mi cartera de criptomonedas | Integrada (06/11) | 2 | 4 (Baja) | 2 | 164 | 12 | 2,0 | 1,9 $ |
| HU-73 | Configurar las criptomonedas | Integrada (06/11) | 3 | 4 (Baja) | 3 | 246 | 18 | 2,7 | 2,9 $ |
| HU-74 | Baccarat | Integrada (Sprint 5) | 4 | 4 (Baja) | 5 | 410 | 29 | 5,0 | 4,8 $ |
| HU-75 | Cobrar antes de tiempo (cash-out) | Integrada (Sprint 5) | 4 | 3 (Media) | 8 | 656 | 47 | 7,6 | 7,8 $ |
| HU-76 | Riesgo de la casa por evento | Integrada (16/11) | 3 | 3 (Media) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-77 | Correo al ganar una apuesta | Integrada (16/11) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-78 | Montar una quiniela | Integrada (16/11) | 3 | 4 (Baja) | 3 | 246 | 18 | 3,1 | 2,9 $ |
| HU-79 | Echar un boleto | Integrada (16/11) | 3 | 4 (Baja) | 3 | 246 | 18 | 3,1 | 2,9 $ |
| HU-80 | Repartir el bote de la quiniela | Integrada (16/11) | 3 | 4 (Baja) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-81 | Crear un equipo fantasy y fichar | Integrada (Sprint 5) | 3 | 4 (Baja) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-82 | Puntos de los jugadores fantasy | Integrada (Sprint 5) | 3 | 4 (Baja) | 5 | 410 | 29 | 4,7 | 4,8 $ |
| HU-83 | Clasificación fantasy | Integrada (Sprint 5) | 1 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-84 | Ranking por tipo de juego | Integrada (16/11) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-85 | Tiempo de carga mínimo | Integrada (15/11) | 3 | 2 (Alta) | 3 | 246 | 18 | 3,1 | 2,9 $ |
| HU-86 | Equipos favoritos | Integrada (Sprint 5) | 3 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-87 | Supercuota | Integrada (Sprint 5) | 3 | 3 (Media) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-88 | Logros | Integrada (Sprint 5) | 2 | 4 (Baja) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-89 | Reto 1 contra 1 | Integrada (Sprint 5) | 3 | 4 (Baja) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-90 | Alerta de cuota | Integrada (Sprint 5) | 3 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-91 | Evolución de la cuota | Integrada (Sprint 5) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-92 | Resumen semanal por correo | Integrada (Sprint 5) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-93 | Publicidad de Better en otras webs | Por integrar (sprint 5 o 6) | 3 | 2 (Alta) | - | - | - | - | - |
| HU-94 | Modo oscuro | Integrada (Sprint 5) | 2 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-95 | Beneficio de la casa por día | Integrada (Sprint 5) | 2 | 4 (Baja) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-96 | Estadísticas por competición | Integrada (Sprint 5) | 2 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-97 | Usar la web solo con el teclado | Integrada (Sprint 5) | 3 | 3 (Media) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-98 | Usar la web con un lector de pantalla | Integrada (Sprint 5) | 4 | 3 (Media) | 5 | 410 | 29 | 4,8 | 4,8 $ |
| HU-99 | Leer bien con poca visión | Integrada (Sprint 5) | 3 | 3 (Media) | 3 | 246 | 18 | 2,9 | 2,9 $ |
| HU-100 | No depender solo del color | Integrada (Sprint 5) | 3 | 3 (Media) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-101 | Menos movimiento | Integrada (Sprint 5) | 2 | 3 (Media) | 2 | 164 | 12 | 1,9 | 1,9 $ |
| HU-102 | Ver la oferta de cash-out de una combinada | Integrada (Sprint 5) | 4 | 3 (Media) | — | — | — | — | — |
| HU-103 | Confirmar el cash-out de toda la combinada | Integrada (Sprint 5) | 4 | 3 (Media) | — | — | — | — | — |
| HU-104 | Consultar el cobro anticipado en el historial | Integrada (Sprint 5) | 3 | 3 (Media) | — | — | — | — | — |
| HU-105 | Bloquear a un amigo en un partido o selección | Integrada (Sprint 5) | 5 | 3 (Media) | — | — | — | — | — |
| HU-106 | Bloquear a toda la liga en un partido o selección | Integrada (Sprint 5) | 4 | 3 (Media) | — | — | — | — | — |
| HU-107 | Aplicar y caducar los bloqueos de la liga | Integrada en parte (Sprint 5) | 5 | 3 (Media) | — | — | — | — | — |
| HU-108 | Consultar la clasificación de una liga | Propuesta | 3 | 3 (Media) | — | — | — | — | — |
| HU-109 | Ordenar la clasificación y consultar mi puesto | Propuesta | 4 | 3 (Media) | — | — | — | — | — |
| HU-110 | Mantener la clasificación limitada a los miembros | Propuesta | 3 | 3 (Media) | — | — | — | — | — |
| HU-111 | Alternar entre mis ligas y la clasificación global | Integrada (Sprint 5) | 4 | 3 (Media) | — | — | — | — | — |
| HU-112 | Consultar la clasificación global de ligas públicas | Integrada (Sprint 5) | 4 | 3 (Media) | — | — | — | — | — |
| HU-113 | Cambiar la visibilidad de una liga | Integrada (Sprint 5) | 5 | 3 (Media) | — | — | — | — | — |
| HU-114 | Exportar mi historial | Integrada (Sprint 5) | 2 | 4 (Baja) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**114 historias**: 46 hechas; 56 integradas en `main` (HU-63 a HU-73 el 06/11, HU-85 el 15/11 y HU-76 a HU-80 y HU-84 el 16/11, del Sprint 4; y del Sprint 5, HU-102 a HU-107 y HU-111 a HU-113 el 20/11, HU-55 a HU-62, HU-74 y HU-81 a HU-83 el 21/11, y HU-75, HU-86 a HU-92, HU-94 a HU-101 y HU-114); 1 por hacer (HU-93); 8 descartadas, y 3 propuestas (HU-108 a HU-110). HU-107 está integrada en parte: los bloqueos no se ven en la página del partido ni en el boleto (CA-107.2), y al empezar o suspenderse el partido no se devuelven (CA-107.4). HU-108 a HU-110 están cubiertas casi del todo por el ranking de cada liga (HU-51). Las integradas están programadas y probadas, pero aún no las ha aceptado el Product Owner en una review, el último punto de la [Definition of Done](../scrum/proceso.md#definition-of-done-una-historia-está-terminada-si). **285 criterios de aceptación** en total: 98 de historias hechas, 161 de las integradas, 3 de la que está por hacer, 13 de las descartadas y 10 de las propuestas. Las propuestas aún no están estimadas ni añadidas como filas independientes al [Product Backlog](../scrum/product-backlog.md).

---

## Creador de apuestas

### HU-01 · Alta de competiciones y equipos con su calificación

**Como** creador de apuestas, **quiero** dar de alta competiciones, equipos y deportistas con su calificación de calidad (0–10), **para** que el sistema tenga la base con la que calcular las cuotas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 1, 2 | 1 (Muy alta) | 1 (it. 1) | Hecha | 8 | 656 | 47 | 7,3 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-01.1 | Doy de alta un equipo con su deporte, sus competiciones y su calificación | Lo guardo | Queda disponible para crear eventos con él. |
| CA-01.2 | Introduzco una calificación fuera del rango 0–10 | Intento guardarla | El sistema la rechaza. |

### HU-02 · Forma reciente de un equipo

**Como** creador de apuestas, **quiero** ajustar manualmente el factor de forma reciente de un equipo (rachas, moral, lesiones…), **para** que las cuotas reflejen su momento actual y no solo su calidad general.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 3 | 2 (Alta) | 1 (it. 6) | Hecha | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-02.1 | Modifico el factor de forma de un equipo | Lo guardo | Se recalculan las cuotas de sus eventos que aún no han empezado. |

### HU-03 · Cuotas automáticas con margen para la casa

**Como** creador de apuestas, **quiero** que las cuotas iniciales (G/E/P) de cada evento se generen automáticamente con un margen para la casa, **para** no tener que fijarlas a mano.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 3 | 2 (Alta) | 1 (it. 2 y 6) | Hecha | 11 (8 + 3) | 902 | 65 | 10 | 10,7 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-03.1 | Creo un evento entre dos equipos registrados | Lo guardo | El sistema propone una cuota para cada resultado a partir de su calidad y su forma. |
| CA-03.2 | El sistema genera o recalcula las cuotas de un evento | Se suman las probabilidades implícitas (1/cuota) | La suma de las probabilidades implícitas (1/cuota) es siempre mayor que 1, para que la casa no pierda a largo plazo. |
| CA-03.3 | Se ha apostado mucho a un resultado | Su cuota baja por el volumen apostado | Esta nunca baja de un mínimo fijado (p. ej. 1,01). |

### HU-04 · Introducir el resultado

**Como** creador de apuestas, **quiero** introducir el resultado final de un evento, **para** que se resuelvan todas las apuestas asociadas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 16, 17 | 1 (Muy alta) | 1 (it. 3) | Hecha | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-04.1 | Un evento ha terminado | Introduzco su resultado y lo confirmo | El evento pasa a "finalizado" y se lanza la resolución de sus apuestas. |
| CA-04.2 | Me he equivocado al introducir el resultado | Lo corrijo | Las apuestas se vuelven a resolver y los saldos se ajustan. |

### HU-05 · Suspender o anular un evento

**Como** creador de apuestas, **quiero** suspender o anular un evento (aplazamiento, error en las cuotas…), **para** que nadie apueste en condiciones incorrectas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 17 | 2 (Alta) | 1 (it. 3 y 5) | Hecha | 4 (3 + 1) | 328 | 24 | 3,8 | 3,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-05.1 | Un evento programado | Lo suspendo | Deja de admitir apuestas nuevas hasta que lo reactive. |
| CA-05.2 | Un evento con apuestas | Lo anulo | Se devuelve a cada usuario el importe que apostó en él. |

### HU-06 · Dinero apostado a cada resultado

**Como** creador de apuestas, **quiero** ver cuánto se ha apostado en cada resultado de un evento, **para** comprobar que el ajuste de cuotas funciona y detectar desequilibrios.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 17 | 2 (Alta) | 1 (it. 3) | Hecha | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-06.1 | Un evento con apuestas | Entro en él desde el panel de gestión | Veo el importe total y el número de apuestas de cada resultado. |

### HU-07 · Límites de apuesta del creador

**Como** creador de apuestas, **quiero** fijar límites de apuesta (importe mínimo y máximo, número máximo de selecciones en una combinada), **para** controlar el riesgo de la casa.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 17 | 2 (Alta) | 1 (it. 4 y 6) | Hecha | 3 (1 + 2) | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-07.1 | Un usuario intenta apostar fuera de esos límites | Confirma la apuesta | El sistema lo impide y le indica el límite. |

### HU-46 · Editar o borrar un evento

**Como** creador de apuestas, **quiero** editar o borrar un evento que he creado mal (fecha, equipos, fase), **para** corregir errores sin tener que anularlo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 29 | 1 (Muy alta) | 1 (it. 8) | Hecha | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-46.1 | Un evento aún no ha empezado | Cambio su fecha, sus equipos o su fase | Se guardan los cambios y las cuotas se recalculan. |
| CA-46.2 | Un evento no tiene apuestas | Lo borro | Desaparece del catálogo. Si tiene apuestas, solo se puede anular (se devuelve el importe). |

### HU-53 · Cuentas de la casa

**Como** creador de apuestas, **quiero** ver un panel con lo que se ha apostado, lo que se ha pagado y el beneficio de la casa, **para** comprobar si las cuotas están bien ajustadas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 35 | 3 (Media) | 1 (it. 11) | Hecha | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-53.1 | Hay apuestas resueltas | Entro en el panel de la casa | Veo el total apostado, el total pagado, el beneficio y el margen real frente al margen teórico. |
| CA-53.2 | Hay apuestas de varios tipos y deportes | Entro en el panel de la casa | Veo el desglose por tipo de apuesta (simples, combinadas y largo plazo) y por deporte. |

### HU-76 · Riesgo de la casa por evento

**Como** creador de apuestas, **quiero** ver cuánto pagaría la casa con cada resultado de un evento, **para** detectar a tiempo los partidos en los que puede perder mucho dinero.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 43 | 3 (Media) | 4 | Integrada (16/11) | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-76.1 | Un evento con apuestas | Abro su ficha en *Gestión* | Veo lo que habría que pagar con cada resultado y lo que le quedaría a la casa. Las apuestas canceladas no cuentan. |
| CA-76.2 | En el peor caso la casa pierde más que el límite de alerta | Abro la ficha del evento | Sale un aviso de riesgo. |
| CA-76.3 | Hay eventos en alerta | Abro el panel de *Gestión* | Esos eventos salen marcados. El límite (2.000 monedas al empezar) se cambia en *Límites de apuesta*. |

El peor caso cuenta las combinadas enteras y supone que las de goles y ambos marcan se cobran con cualquier resultado. Una combinada con otra selección ya fallada no cuenta, porque ya no se va a pagar.

**Pruebas:** `RiesgoWebTest` (CA-76.1 a CA-76.3).

### HU-87 · Supercuota

**Como** creador de apuestas, **quiero** mejorar durante unas horas la cuota de un resultado con un importe máximo por apuesta, **para** atraer apuestas a un partido.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 49 | 3 (Media) | 5 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-87.1 | Un evento que admite apuestas | Publico una supercuota con resultado, cuota, importe máximo y horas | Queda vigente esas horas o hasta que empiece el partido. Tiene que mejorar la cuota normal sin pasar del doble; una nueva sustituye a la anterior. |
| CA-87.2 | Hay una supercuota | Un usuario hace una simple a ese resultado | Se le aplica la supercuota hasta el importe máximo; por encima no se admite. En las combinadas vale la cuota normal. |
| CA-87.3 | Hay supercuotas vigentes | Abro la portada o la página del partido | Salen destacadas, con su importe máximo y cuándo acaban. |

Si después se cambia el importe de una apuesta hecha con supercuota, se recalcula con la cuota normal, como cualquier otra (HU-27).

**Pruebas:** `SupercuotaWebTest`.

### HU-95 · Beneficio de la casa por día

**Como** creador de apuestas, **quiero** ver el beneficio de la casa en cada uno de los últimos 30 días, **para** ver la tendencia.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 55 | 4 (Baja) | 5 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-95.1 | Se ha jugado en los últimos 30 días | Abro el panel de la casa | Veo el beneficio de cada día en un gráfico y en una tabla, y el total. |
| CA-95.2 | Un día cualquiera | Los jugadores pagan y cobran en apuestas, casino, predicciones o quiniela | Lo que pagan suma y lo que cobran resta. Las recargas, el saldo de bienvenida, los retos y la cripto no cuentan. |

**Pruebas:** `BeneficioDiarioWebTest`.

---

## General

### HU-08 · Catálogo sin registrarse

**Como** visitante, **quiero** consultar el catálogo de eventos y sus cuotas sin estar registrado, **para** decidir si me interesa crear una cuenta.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 9 | 1 (Muy alta) | 1 (it. 1) | Hecha | 2 | 164 | 12 | 2,1 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-08.1 | No he iniciado sesión | Entro en la web | Puedo ver los eventos y sus cuotas. |
| CA-08.2 | No he iniciado sesión | Intento apostar | Se me redirige al inicio de sesión o al registro. |

### HU-09 · Web adaptada a móvil

**Como** usuario, **quiero** que la web se adapte a móvil y a ordenador, **para** poder apostar desde cualquier dispositivo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| Requisito no funcional | 5 (Muy baja) | 1 (it. 9) | Hecha | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-09.1 | Una pantalla de 390 px de ancho | Abro cualquier página | No aparece barra de desplazamiento horizontal. |
| CA-09.2 | Un móvil | Hago una apuesta simple de principio a fin | La completo sin hacer zoom ni desplazarme en horizontal. |

### HU-10 · Juego responsable

**Como** usuario, **quiero** poder fijarme límites de apuesta (diarios o semanales) o una pausa temporal, **para** jugar de forma responsable.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 27 | 4 (Baja) | 1 (it. 9) | Hecha | 6 (5 + 1) | 492 | 35 | 5,6 | 5,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-10.1 | He fijado un límite diario | Lo alcanzo | No puedo apostar más hasta el día siguiente. |
| CA-10.2 | He activado una pausa | Intento apostar antes de que termine | No puedo apostar hasta que termine, aunque sí puedo consultar mi cuenta. |

### HU-85 · Tiempo de carga mínimo

**Como** visitante o usuario, **quiero** que las páginas se abran enseguida, también la primera vez después de un rato sin que nadie entre en la web, **para** no tener que esperar ni abandonarla.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 41 | 2 (Alta) | 4 | Integrada (15/11) | 3 | 246 | 18 | 3,1 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-85.1 | Nadie ha entrado en la web en más de 15 minutos | Un visitante la abre | La página se abre como siempre, sin esperar a que el servidor vuelva a arrancar: la web no se ha apagado por falta de visitas. |
| CA-85.2 | Mi navegador acepta páginas comprimidas | Pido una página | Me llega comprimida, así que descargo menos, y se ve igual. Si mi navegador no las acepta, me llega sin comprimir. |
| CA-85.3 | Ya he visitado la web | Abro otra página | Mi navegador no vuelve a descargar la hoja de estilos; solo la descarga otra vez si ha cambiado. |

**Requisito no funcional (rendimiento).** Se escribe como historia porque quien nota la diferencia es quien entra en la web. **Cómo se cumple:** la aplicación se visita a sí misma (`/ping`) cada 10 minutos para que Render no la apague, las páginas viajan comprimidas con gzip y la hoja de estilos lleva la huella de su contenido en el nombre y se guarda un año en la caché del navegador.

Medido en local antes y después del cambio: la portada pasa de 4,5 KB a 1,5 KB, la lista de equipos de 122 KB a 6,5 KB y la hoja de estilos de 50 KB a 10 KB, que además se descarga una sola vez. Antes, la primera visita tras 15 minutos sin uso esperaba de 30 a 60 segundos a que Render volviera a arrancar la web.

*Redactada de nuevo el 27/11:* antes el rol era «cliente», que no es ninguno de los [actores](#actores), pedía «el menor tiempo posible», que no se puede comprobar, y los criterios describían cómo está hecho en lugar de lo que nota quien visita la web. Las pruebas no cambian.

### HU-94 · Modo oscuro

**Como** usuario, **quiero** cambiar la web a colores oscuros, **para** usarla de noche.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 54 | 4 (Baja) | 5 | Integrada (Sprint 5) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-94.1 | Cualquier página, con sesión o sin ella | Pulso la luna de la cabecera | La web pasa a colores oscuros y vuelvo a la misma página; con el sol, vuelven los claros. |
| CA-94.2 | He elegido el modo oscuro | Vuelvo otro día | Se recuerda: se guarda en una cookie durante un año. |

Es un enlace y no un formulario para no crear una sesión a cada visitante.

**Pruebas:** `TemaWebTest`.

---

## Gestión de usuarios

### HU-11 · Registro

**Como** usuario, **quiero** registrarme y crear un perfil, **para** poder acceder a la aplicación con una cuenta propia.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 5 | 1 (Muy alta) | 1 (it. 1) | Hecha | 3 | 246 | 18 | 3,1 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-11.1 | Relleno el formulario de registro con un email y una contraseña válidos | Lo envío | Se crea mi cuenta y mi perfil. |
| CA-11.2 | Intento registrarme con un email ya existente | Lo envío | El sistema me avisa de que ya hay una cuenta con ese email. |
| CA-11.3 | Escribo un nombre con palabras ofensivas, aunque cambie letras por números | Envío el registro | El sistema no crea la cuenta y me pide otro nombre. |

### HU-12 · Inicio de sesión

**Como** usuario, **quiero** iniciar sesión de forma segura, **para** proteger el acceso a mi cuenta y a mi saldo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 5, 8 | 1 (Muy alta) | 1 (it. 1) | Hecha | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-12.1 | Introduzco mi email y contraseña correctos | Inicio sesión | Accedo a mi cuenta. |
| CA-12.2 | Los datos de acceso son incorrectos | Inicio sesión | El sistema deniega el acceso con un mensaje de error genérico. |

### HU-13 · Consultar el saldo

**Como** usuario, **quiero** consultar mi saldo de moneditas virtuales en cualquier momento, **para** tener control de cuánto tengo disponible para apostar.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 4, 12 | 1 (Muy alta) | 1 (it. 1) | Hecha | 2 | 164 | 12 | 2,1 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-13.1 | Soy un usuario registrado | Entro en mi cuenta | Veo mi saldo actual de moneditas. |
| CA-13.2 | Mi saldo cambia (apuesta o recarga) | Lo vuelvo a consultar | El valor está actualizado. |

### HU-14 · Saldo de bienvenida y recargas

**Como** usuario, **quiero** recibir una recarga inicial de moneditas al registrarme (y recargas periódicas gratuitas), **para** poder seguir jugando sin aportar dinero real.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 4 | 2 (Alta) | 1 (it. 1) | Hecha | 2 | 164 | 12 | 2,1 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-14.1 | Soy un usuario nuevo | Completo el registro | Se me asigna automáticamente el saldo de bienvenida. |
| CA-14.2 | Ha pasado el periodo definido desde mi última recarga gratuita | Inicio sesión o abro mi cuenta | Recibo automáticamente una nueva recarga. |

### HU-54 · Nombres ofensivos

**Como** usuario, **quiero** que el resto de usuarios tengan nombres adecuados, **para** no sentirme ofendido o insultado.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| - | 4 (Baja) | 2 | Hecha | 2 | 153 | 12 | 1,7 | 1,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-54.1 | Un usuario que introduce un username considerado "prohibido" | Crea la cuenta | Se le informa de que el nombre elegido puede resultar ofensivo y se le obliga a cambiarlo. |

### HU-55 · Movimientos del saldo

**Como** usuario, **quiero** ver cada movimiento de mi saldo con su concepto, **para** saber de dónde sale y adónde va cada monedita.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 36 | 3 (Media) | 3 | Integrada (Sprint 5) | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-55.1 | Una operación cambia mi saldo (apuesta, premio, devolución, corrección, recarga o baja) | Se confirma | Queda un movimiento con su tipo, el importe con signo, el saldo resultante, el concepto y la fecha. |
| CA-55.2 | Tengo movimientos | Abro *Mi cuenta → Movimientos* | Los veo del más reciente al más antiguo, y el saldo resultante del último coincide con mi saldo. |
| CA-55.3 | Una operación falla (saldo insuficiente, límites…) | Se deshace | No queda ningún movimiento ni cambia mi saldo. |

Es la base común de la ampliación: el casino y los mercados de predicción apuntarán aquí sus cobros y pagos con sus propios tipos de movimiento.

### HU-15 · Cuenta bancaria

**Como** usuario, **quiero** añadir y gestionar los datos de una cuenta bancaria (real o ficticia), **para** poder retirar mi saldo cuando lo desee.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 25 | 5 (Muy baja) | — | Descartada (Won't) | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-15.1 | No tengo ninguna cuenta bancaria vinculada | Añado una | Queda guardada asociada a mi perfil. |
| CA-15.2 | Ya tengo una cuenta vinculada | La edito o la sustituyo por otra | Se actualiza la que se usará para futuras retiradas. |

### HU-16 · Retirar el saldo

**Como** usuario, **quiero** retirar mi saldo disponible a mi cuenta bancaria vinculada, **para** poder disponer de mis ganancias fuera de la aplicación.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 25 | 5 (Muy baja) | — | Descartada (Won't) | 3 | 246 | 18 | 3,1 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-16.1 | Tengo saldo disponible (no comprometido en apuestas activas) y una cuenta vinculada | Solicito una retirada por un importe igual o menor a ese saldo | Se descuenta de mi saldo y la solicitud queda registrada. |
| CA-16.2 | Solicito retirar más saldo del disponible | Confirmo la operación | El sistema la rechaza. |
| CA-16.3 | Se completa una retirada | Consulto mi historial | Aparece registrada con su importe y fecha. |

### HU-17 · Recuperar la contraseña

**Como** usuario, **quiero** recuperar el acceso si olvido mi contraseña, **para** no perder mi cuenta ni mi saldo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 22 | 3 (Media) | 2 | Hecha | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-17.1 | Pulso "he olvidado mi contraseña" e introduzco mi email | Lo envío | Recibo un enlace para crear una contraseña nueva. |
| CA-17.2 | El enlace ha caducado o ya se ha usado | Lo abro | El sistema no permite cambiar la contraseña. |

### HU-18 · Eliminar la cuenta

**Como** usuario, **quiero** eliminar mi cuenta, **para** que se borren mis datos personales si dejo de usar la aplicación.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 22 | 2 (Alta) | 1 (it. 9) | Hecha | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-18.1 | Tengo apuestas activas | Intento eliminar la cuenta | El sistema me avisa de que las perderé y me pide confirmación. |
| CA-18.2 | He pedido eliminar mi cuenta | Confirmo la eliminación | Ya no puedo iniciar sesión y mis datos personales se borran. |

### HU-47 · Cambiar nombre y contraseña

**Como** usuario, **quiero** cambiar mi nombre y mi contraseña desde *Mi cuenta*, **para** mantener mis datos al día.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 30 | 3 (Media) | 1 (it. 8) | Hecha | 2 | 164 | 12 | 2,2 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-47.1 | Introduzco mi contraseña actual y una nueva válida | La guardo | La siguiente vez entro con la nueva. |
| CA-47.2 | La contraseña actual no es correcta | Intento cambiarla | El sistema no lo permite. |
| CA-47.3 | Escribo un nombre nuevo con palabras ofensivas | Lo guardo | El sistema no lo cambia y me pide otro nombre. |

---

## Catálogo de eventos deportivos

### HU-19 · Catálogo por deporte y fecha

**Como** usuario, **quiero** ver una lista de eventos deportivos disponibles organizados por deporte y fecha, **para** poder apostar a tiempo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 9 | 1 (Muy alta) | 1 (it. 1) | Hecha | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-19.1 | Hay eventos disponibles | Entro en el catálogo | Los veo agrupados por deporte y ordenados por fecha. |
| CA-19.2 | Un evento ya ha empezado o terminado | Consulto el catálogo | Ya no aparece como disponible para apostar. |

### HU-20 · Cuotas de cada resultado

**Como** usuario, **quiero** ver las cuotas asociadas a cada resultado posible de un evento, **para** apostar cuando el riesgo-recompensa compense.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 3, 9 | 2 (Alta) | 1 (it. 2) | Hecha | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-20.1 | Entro en un evento | Lo consulto | Veo una cuota para cada resultado posible (G/E/P). |
| CA-20.2 | Las cuotas cambian por el volumen apostado | Vuelvo a consultar el evento | Veo las cuotas actualizadas. |

### HU-21 · Cargar eventos a mano o desde la API

**Como** administrador, **quiero** cargar o actualizar eventos y cuotas manualmente o desde una fuente externa, **para** corregir errores y mantener los datos al día.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 2, 13, 17 | 2 (Alta) | 1 (it. 5) | Hecha | 8 | 656 | 47 | 7,3 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-21.1 | Doy de alta o edito un evento | Lo guardo | Se refleja de inmediato en el catálogo que ve el usuario. |
| CA-21.2 | La fuente externa está disponible | Se sincroniza | Los eventos y cuotas se actualizan sin intervención manual. *(fuera del MVP)*. |

### HU-22 · Búsqueda y filtros

**Como** usuario, **quiero** buscar un equipo o una competición y filtrar el catálogo, **para** encontrar rápido el evento que me interesa.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 21 | 3 (Media) | 1 (it. 2) | Hecha | 3 | 246 | 18 | 3,1 | 2,9 $ |

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

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 6 | 1 (Muy alta) | 1 (it. 2) | Hecha | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-23.1 | Tengo saldo suficiente | Confirmo una apuesta | El importe se descuenta de mi saldo y la apuesta queda registrada como activa. |
| CA-23.2 | No tengo saldo suficiente | Intento confirmar la apuesta | El sistema me lo impide y me avisa. |

### HU-24 · Apuestas activas

**Como** usuario, **quiero** ver el estado de mis apuestas activas, **para** saber en todo momento cuánto tengo comprometido y en qué apuestas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 10, 11 | 2 (Alta) | 1 (it. 2) | Hecha | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-24.1 | Tengo apuestas activas | Entro en mi sección de apuestas | Veo cada una con su evento, cuota e importe. |

### HU-25 · Resolución automática

**Como** usuario, **quiero** que mis apuestas se resuelvan automáticamente en cuanto termina el evento, **para** recibir mis ganancias (o ver la pérdida) sin tener que reclamarlo yo mismo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 16 | 2 (Alta) | 1 (it. 3 y 5) | Hecha | 8 (5 + 3) | 656 | 47 | 7,5 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-25.1 | Un evento tiene apuestas activas | Pasa a "finalizado" con un resultado | Todas sus apuestas asociadas pasan a "ganada" o "perdida" según corresponda. |
| CA-25.2 | Tengo una apuesta activa | Pasa a "ganada" | Mi saldo se actualiza automáticamente con la ganancia. |
| CA-25.3 | Se resuelve una apuesta | Consulto mi historial | Veo el resultado sin haber hecho nada manualmente. |

### HU-26 · Cancelar una apuesta

**Como** usuario, **quiero** cancelar una apuesta activa antes de que empiece el evento, **para** recuperar el importe si cambio de opinión.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 14 | 2 (Alta) | 1 (it. 2) | Hecha | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-26.1 | El evento aún no ha empezado | Cancelo la apuesta | Se me devuelve el importe al saldo. |
| CA-26.2 | El evento ya ha empezado | Intento cancelarla | El sistema no lo permite. |

### HU-27 · Cambiar el importe

**Como** usuario, **quiero** modificar el importe de una apuesta activa antes de que empiece el evento, **para** ajustar lo que arriesgo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 14 | 2 (Alta) | 1 (it. 7) | Hecha | 2 | 164 | 12 | 2,1 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-27.1 | Subo el importe y tengo saldo suficiente | Confirmo | Se descuenta la diferencia y se aplica la cuota vigente en ese momento. |
| CA-27.2 | Bajo el importe | Confirmo | Se me devuelve la diferencia al saldo. |

### HU-52 · Doble oportunidad, goles y ambos marcan

**Como** usuario, **quiero** apostar a más cosas de un partido de fútbol (doble oportunidad, más o menos de 2,5 goles y ambos marcan), **para** tener más opciones que el 1X2.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 34 | 3 (Media) | 1 (it. 11) | Hecha | 8 | 656 | 47 | 7,5 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-52.1 | Hay un partido de fútbol programado | Entro en su página | Veo las cuotas de doble oportunidad (1X, X2, 12), de más/menos de 2,5 goles y de ambos marcan (sí/no). |
| CA-52.2 | Estoy en un partido de fútbol | Añado una de estas selecciones al boleto | Puedo apostarla sola o en una combinada. |
| CA-52.3 | Hay apuestas de estos tipos en un partido | El creador introduce el marcador final | Estas selecciones se resuelven solas; si solo se conoce el ganador, las de goles se anulan (cuota 1,00). |

### HU-75 · Cobrar antes de tiempo (cash-out)

**Como** usuario, **quiero** cobrar una apuesta antes de que terminen sus partidos, **para** asegurarme una ganancia o limitar lo que pierdo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 42 | 3 (Media) | 4 | Integrada (Sprint 5) | 8 | 656 | 47 | 7,6 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-75.1 | Una apuesta activa, simple o combinada, y todos sus eventos siguen programados y futuros | Abro *Mis apuestas* | Veo «Cobrar ya» con el valor actual de la apuesta completa: importe × producto de (cuota aceptada al apostar / cuota actual) para todas sus selecciones, sin pasar de la ganancia potencial original. Recién hecha vale lo apostado. Para una combinada se aplican las reglas detalladas en HU-102 a HU-104. |
| CA-75.2 | Ha empezado, se ha suspendido o se ha anulado cualquiera de los eventos de la apuesta | Abro *Mis apuestas* | No se ofrece cobrar la apuesta. |
| CA-75.3 | He cobrado una apuesta | Después se juegan sus partidos | La apuesta queda cerrada con el estado «Cobro anticipado» y el resultado ya no cambia el importe recibido. |
| CA-75.4 | Las cuotas cambian mientras decido | Pulso «Cobrar ya» | No se cobra y se me enseña la oferta nueva. |

La cuota actual no cuenta el dinero que ha apostado el propio jugador, para que apostar y cobrar al momento no dé ganancia. El cash-out cierra la apuesta completa: no se pueden retirar selecciones sueltas de una combinada. Una apuesta cobrada cuenta en el balance y en el ranking, pero no como acierto.

Una simple hecha con supercuota (HU-87) no tiene cash-out: su cuota aceptada es mayor que la normal desde que se apuesta, y valdría más de lo apostado nada más apostar (#135).

**Pruebas:** `CashOutYBloqueoLigaWebTest`. En `main` desde #128 (combinadas, con la oferta de HU-102 y el cobro de HU-103) y #153 (simples).

### HU-102 · Ver la oferta de cash-out de una combinada

**Como** usuario, **quiero** ver cuánto recibiría por cerrar una apuesta combinada completa antes de que empiece cualquiera de sus eventos, **para** decidir si aseguro el importe ofrecido.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 42 (ampliación de HU-75) | 3 (Media) | 5 | Integrada (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-102.1 | Tengo una combinada activa con dos o más selecciones y todos sus eventos siguen programados y no han empezado | Abro *Mis apuestas* | Veo una oferta de cash-out para la apuesta principal completa, no una oferta independiente por selección. |
| CA-102.2 | La oferta se calcula para una combinada que todavía no ha empezado | El sistema calcula su valor | Usa importe × producto(cuota aceptada al apostar / cuota actual) para todas las selecciones, redondea a dos decimales, no supera la ganancia potencial original y muestra el importe total que se abonará. |
| CA-102.3 | La combinada acaba de hacerse y ninguna cuota ha cambiado | Consulto su oferta | La oferta inicial coincide con el importe apostado; la propia apuesta no se cuenta al calcular las cuotas actuales. |
| CA-102.4 | Ha empezado, se ha suspendido o se ha anulado cualquiera de los eventos de la combinada | Abro *Mis apuestas* | No veo la acción de cash-out para esa combinada, aunque los demás eventos aún no hayan empezado. |

### HU-103 · Confirmar el cash-out de toda la combinada

**Como** usuario con una combinada activa, **quiero** confirmar una oferta de cash-out vigente, **para** recibirla una sola vez y cerrar todas las selecciones juntas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 42 (ampliación de HU-75) | 3 (Media) | 5 | Integrada (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-103.1 | La combinada conserva la oferta mostrada y todos sus eventos siguen programados y futuros | Pulso «Cobrar ya» | Se cierra la apuesta principal completa y se abona exactamente el importe ofrecido. |
| CA-103.2 | Cambió una cuota desde que vi la oferta | Confirmo el cobro | No se cierra ni se abona nada; se muestra la oferta recalculada y tengo que confirmarla de nuevo. |
| CA-103.3 | Un evento empieza, se suspende o se anula mientras confirmo | Se procesa la solicitud | El sistema vuelve a comprobar todos los eventos; si alguno ya no cumple las condiciones, rechaza el cash-out sin abono parcial. |
| CA-103.4 | Envío dos veces la misma confirmación, por ejemplo al pulsar dos veces o reintentar la petición | El servidor procesa las solicitudes | La combinada se cierra y el importe se abona como máximo una vez. |

### HU-104 · Consultar el cobro anticipado en el historial

**Como** usuario que ha cerrado una combinada con cash-out, **quiero** reconocerla en mi saldo, mis apuestas y mis estadísticas, **para** entender cómo terminó y cuánto gané o perdí.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 42 (ampliación de HU-75) | 3 (Media) | 5 | Integrada (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-104.1 | Cobré anticipadamente una combinada | Abro *Mis apuestas* o el historial | La veo con el estado «Cobro anticipado», su importe original, el importe abonado y la fecha del cobro. |
| CA-104.2 | Una combinada ya se cerró con cash-out | Se introducen después los resultados de sus eventos | Sus selecciones no vuelven a resolverse ni cambian el importe que recibí. |
| CA-104.3 | Se calculan el saldo, el balance y las estadísticas | La combinada se cerró con cash-out | El abono y el resultado neto (importe abonado menos importe apostado) cuentan en el balance; no se clasifica como acierto ni como apuesta ganada. |

Estas tres propuestas concretan HU-75 para combinadas y quedan fuera de la [matriz de trazabilidad](trazabilidad.md) hasta que se acepten y se implementen.

---

## Apuestas combinadas

### HU-28 · Boleto de combinadas

**Como** usuario, **quiero** añadir a un mismo boleto selecciones de varios eventos, **para** crear mi propia apuesta combinada.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 15, 18 | 2 (Alta) | 1 (it. 4) | Hecha | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-28.1 | Tengo selecciones en el boleto | Añado otra de un evento distinto | Se añade al boleto. |
| CA-28.2 | Intento añadir dos selecciones del mismo evento | Las añado | El sistema no lo permite y me avisa. |
| CA-28.3 | Tengo selecciones en el boleto | Quito una de ellas | Desaparece y, si solo queda una, el boleto pasa a ser una apuesta simple. |

### HU-29 · Multiplicador total y ganancia potencial

**Como** usuario, **quiero** que el sistema calcule el multiplicador total del boleto y la ganancia potencial, **para** saber cuánto puedo ganar antes de confirmar.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 15, 18 | 2 (Alta) | 1 (it. 4) | Hecha | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-29.1 | El boleto tiene varias selecciones | Lo abro | El multiplicador total es el producto de las cuotas de todas ellas. |
| CA-29.2 | Tengo un boleto con selecciones | Introduzco un importe | Veo la ganancia potencial (importe × multiplicador total) antes de confirmar. |
| CA-29.3 | Tengo un boleto con selecciones | Cambia la cuota de alguna antes de confirmar | El multiplicador se recalcula y se me pide que acepte la nueva cuota. |

### HU-30 · Resolución de combinadas

**Como** usuario, **quiero** que mi combinada se resuelva según el resultado de todas sus selecciones, **para** cobrar solo si acierto todas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 16 | 2 (Alta) | 1 (it. 4) | Hecha | 5 | 410 | 29 | 4,9 | 4,8 $ |

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

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 20 | 3 (Media) | 1 (it. 7) | Hecha | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-31.1 | Un equipo con partidos jugados | Entro en su ficha | Veo sus últimos 5 resultados y su posición en cada competición. |
| CA-31.2 | Estoy viendo un evento | Pulso sobre uno de los equipos | Accedo a su ficha. |

### HU-32 · Gráfico de evolución

**Como** usuario, **quiero** ver la evolución de un equipo a lo largo de la temporada en un gráfico, **para** detectar tendencias (rachas, mejora o bajón).

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 20 | 4 (Baja) | 1 (it. 9) | Hecha | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-32.1 | Entro en la ficha de un equipo | Abro la pestaña de evolución | Veo un gráfico con sus puntos o su posición jornada a jornada. |

### HU-49 · Plantilla y alineación probable

**Como** usuario, **quiero** ver la plantilla de cada equipo con la nota de cada jugador y su alineación probable, **para** saber con quién juega antes de apostar.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 32 | 4 (Baja) | 1 (it. 10) | Hecha | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-49.1 | Un equipo de fútbol con jugadores suficientes | Entro en su ficha | Veo la alineación probable (4-3-3) en un campo, con los jugadores de mejor nota en cada posición. |
| CA-49.2 | Un equipo con jugadores | Entro en su ficha | Veo la plantilla agrupada por posición, con dorsal, nacionalidad, edad y nota. |

### HU-50 · Plantillas desde la API y a mano

**Como** creador de apuestas, **quiero** descargar las plantillas reales desde la API y añadir, puntuar o quitar jugadores a mano, **para** mantenerlas al día.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 32 | 3 (Media) | 1 (it. 10) | Hecha | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-50.1 | La API está configurada | Pulso *Descargar plantillas* | Se crean o actualizan los jugadores de nuestros equipos sin cambiar las notas que ya puse. |
| CA-50.2 | Un jugador ha dejado el equipo en la API | Se descargan las plantillas | Desaparece de la plantilla; los jugadores añadidos a mano no se tocan. |

### HU-33 · Cara a cara

**Como** usuario, **quiero** comparar cara a cara a los dos rivales de un evento, **para** decidir mi apuesta con más información.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 20 | 3 (Media) | 1 (it. 7) | Hecha | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-33.1 | Entro en un evento | Pulso "comparar" | Veo el historial de enfrentamientos entre ambos y sus estadísticas principales una junto a otra. |
| CA-33.2 | Dos equipos sin enfrentamientos previos | Entro en el partido entre ellos | Se indica y se muestran solo las estadísticas de cada uno. |

---

## Historial y estadísticas personales

### HU-34 · Historial

**Como** usuario, **quiero** ver mi historial completo de apuestas con sus resultados, **para** poder repasar mis decisiones pasadas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 19 | 2 (Alta) | 1 (it. 3) | Hecha | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-34.1 | Tengo apuestas resueltas | Entro en mi historial | Veo cada una con su evento, cuota, importe y resultado. |

### HU-35 · Estadísticas personales

**Como** usuario, **quiero** ver estadísticas básicas de mi actividad (porcentaje de aciertos, rentabilidad), **para** saber si mi balance global es positivo o negativo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 19 | 2 (Alta) | 1 (it. 3) | Hecha | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-35.1 | Tengo al menos una apuesta resuelta | Entro en mis estadísticas | Veo mi porcentaje de aciertos y mi rentabilidad acumulada. |

---

## Social y notificaciones

### HU-36 · Ranking

**Como** usuario, **quiero** ver un ranking de usuarios ordenado por ganancias o por porcentaje de aciertos, **para** poder compararme con mis compañeros.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 24 | 2 (Alta) | 1 (it. 2 y 3) | Hecha | 5 (3 + 2) | 410 | 29 | 5,0 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-36.1 | Hay usuarios con actividad registrada | Entro en el ranking | Veo una clasificación ordenada de todos ellos. |
| CA-36.2 | Estoy viendo el ranking | Cambio el criterio de ordenación (ganancias, aciertos, saldo) | La lista se reordena en consecuencia. |
| CA-36.3 | Se resuelve una apuesta | Vuelvo a consultar el ranking | Está actualizado. |
| CA-36.4 | No estoy entre los primeros puestos | Consulto el ranking | Veo mi posición destacada igualmente. |

### HU-88 · Logros

**Como** usuario, **quiero** ganar insignias por mis hitos, **para** que se vean en mi perfil público.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 50 | 4 (Baja) | 5 | Integrada (Sprint 5) | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-88.1 | Un jugador | Abro su perfil público | Veo sus 8 logros, los conseguidos y los que le faltan, y cuántos lleva. |
| CA-88.2 | Cumplo un hito: primera apuesta, primer acierto, combinada ganadora, 5 ganadas seguidas, ganar con cuota de 5 o más, ir en positivo con 10 resueltas, ganar una quiniela o acertar un mercado de predicción | Se resuelve | El logro sale conseguido. Una perdida corta la racha. |

Se calculan con lo que ya hay guardado, así que no hay que apuntarlos aparte.

**Pruebas:** `LogrosWebTest`.

### HU-84 · Ranking por tipo de juego

**Como** usuario, **quiero** filtrar el ranking por tipo de juego (deportes, predicciones, casino, cripto, memecoins y quiniela), **para** compararme con quienes juegan a lo mismo que yo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 47 | 4 (Baja) | 4 | Integrada (16/11) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-84.1 | Elijo un tipo de juego en el ranking | Se carga | Solo salen los que han jugado a ese tipo, con sus estadísticas de ese tipo. |
| CA-84.2 | He elegido un tipo de juego | Ordeno el ranking | No se puede ordenar por saldo, que es común a todos los juegos, y en cripto no hay porcentaje de aciertos. |

Depende del casino, los mercados de predicción y la cartera (HU-56 a HU-73) y de la quiniela (HU-78 a HU-80). El casino (HU-56 a HU-59 y HU-74) llegó a `main` después, el 21/11, y todavía no sale como tipo de juego en el ranking.

En cripto y memecoins el beneficio es lo vendido más lo que vale hoy lo que se tiene, menos lo comprado. En predicciones cuentan los mercados resueltos, y en la quiniela las repartidas, sin los boletos devueltos.

**Pruebas:** `RankingPorJuegoWebTest` (CA-84.1 y CA-84.2).

### HU-37 · Avisos

**Como** usuario, **quiero** recibir una notificación cuando se resuelve una de mis apuestas, **para** saber si he ganado o perdido sin tener que revisar la aplicación constantemente.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 23 | 4 (Baja) | 1 (it. 7) | Hecha | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-37.1 | Tengo una apuesta activa | Se resuelve | Recibo una notificación automática. |
| CA-37.2 | Una de mis apuestas se ha resuelto | Abro la notificación | Indica claramente el evento, el resultado y si gané o perdí. |
| CA-37.3 | He descartado una notificación | Entro en mi historial de notificaciones | Puedo consultarla igualmente. |

### HU-77 · Correo al ganar una apuesta

**Como** usuario, **quiero** recibir un correo cuando gano una apuesta, **para** enterarme aunque no entre en la web.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 44 | 4 (Baja) | 4 | Integrada (16/11) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-77.1 | Tengo los correos activados | Una apuesta mía se resuelve como ganada | Recibo un correo con el mismo texto que el aviso de la web y el enlace a *Mis apuestas*. Si pierdo, no llega ninguno. |
| CA-77.2 | He desactivado los correos en *Mi cuenta* | Gano una apuesta | No recibo correo; el aviso de la web sigue saliendo. |

El correo se envía cuando el resultado ya está guardado, para no avisar de premios que se deshacen. Al corregir un resultado no se envía. Sin servidor de correo, se escribe en la consola, como el de recuperar la contraseña (HU-17).

**Pruebas:** `CorreoPremioWebTest` (CA-77.1 y CA-77.2) y `CorreoTrasGuardarTest` (el correo sale al guardar y no sale si se deshace).

### HU-92 · Resumen semanal por correo

**Como** usuario, **quiero** recibir cada lunes un correo con mi semana, **para** saber cómo voy sin entrar en la web.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 52 | 4 (Baja) | 5 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-92.1 | He jugado en los últimos 7 días | Llega el lunes | Recibo un correo con lo jugado, lo cobrado, mi saldo, mi puesto en el ranking y el enlace a *Mis apuestas*. Si no he jugado, no llega. |
| CA-92.2 | Lo he desactivado en *Mi cuenta* | Llega el lunes | No me llega. |

Sale a las 9:00 (hora de Madrid). Sin servidor de correo, se escribe en la consola, como el de HU-77.

**Pruebas:** `ResumenSemanalTest` y `CorreoResumenTest`.

### HU-89 · Reto 1 contra 1

**Como** miembro de una liga, **quiero** retar a un compañero en un partido con el mismo importe cada uno, **para** que el que acierte se lleve todo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 51 | 4 (Baja) | 5 | Integrada (Sprint 5) | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-89.1 | Comparto liga con alguien | Le reto en un partido con un resultado y un importe | Se me cobra el importe y le llega un aviso. Solo se reta a compañeros de liga. |
| CA-89.2 | Me han retado | Acepto con otro resultado, o rechazo | Si acepto, se me cobra lo mismo; si rechazo, o el retador lo retira, se le devuelve al retador. |
| CA-89.3 | Termina el partido | Se resuelve | El que acierta se lleva los dos importes. Si no acierta ninguno, se anula el partido o el reto no se había aceptado, se devuelve a cada uno lo suyo. |

La casa no se queda nada. Un evento con retos no se puede borrar, solo anular.

**Pruebas:** `RetoWebTest` y `RetoSinTransaccionTest`.

### HU-38 · Seguir a otro usuario

**Como** usuario, **quiero** buscar y seguir a otro usuario desde su perfil, **para** poder ver después su actividad.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 26 | 5 (Muy baja) | — | Descartada (Won't) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-38.1 | Encuentro a otro usuario | Pulso "seguir" en su perfil | Empiezo a seguirle. |

### HU-39 · Seguidos y seguidores

**Como** usuario, **quiero** ver una lista de a quién sigo y quién me sigue, **para** gestionar mis conexiones dentro de la app.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 26 | 5 (Muy baja) | — | Descartada (Won't) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-39.1 | Sigo a otros usuarios | Entro en mi lista de conexiones | Veo a quién sigo y quién me sigue a mí. |

### HU-40 · Actividad de los usuarios que sigo

**Como** usuario, **quiero** consultar el historial de apuestas públicas y las estadísticas generales de los usuarios que sigo, **para** comparar mis resultados con los suyos.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 26 | 5 (Muy baja) | — | Descartada (Won't) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-40.1 | Sigo a un usuario | Entro en su perfil | Veo su historial de apuestas públicas y sus estadísticas generales, respetando el nivel de privacidad definido. |

### HU-41 · Feed de actividad

**Como** usuario, **quiero** ver un feed de actividad con las últimas apuestas resueltas de los usuarios que sigo, **para** estar al tanto sin visitar cada perfil.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 26 | 5 (Muy baja) | — | Descartada (Won't) | 5 | 410 | 29 | 4,5 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-41.1 | Sigo a varios usuarios | Entro en la sección de actividad | Veo un feed con sus últimas apuestas resueltas. |

### HU-42 · Privacidad del perfil

**Como** usuario, **quiero** elegir el nivel de privacidad de mi perfil (público, solo seguidores o privado), **para** decidir quién ve mis apuestas y mis estadísticas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 26 | 5 (Muy baja) | — | Descartada (Won't) | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-42.1 | Mi perfil es privado | Otro usuario entra en él | No ve mi historial ni mis estadísticas. |
| CA-42.2 | Mi perfil es "solo seguidores" | Alguien que no me sigue entra en él | No ve mi historial. |

### HU-43 · Copiar una apuesta

**Como** usuario, **quiero** copiar a mi boleto una apuesta pendiente de un usuario al que sigo, **para** apostar lo mismo que él.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 26 | 5 (Muy baja) | — | Descartada (Won't) | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-43.1 | Veo una apuesta pendiente de un perfil al que tengo acceso | Pulso "copiar" | Sus selecciones se añaden a mi boleto con las cuotas actuales. |
| CA-43.2 | Alguno de los eventos de la apuesta que quiero copiar ya ha empezado | La copio a mi boleto | Esa selección no se copia y se me avisa. |

### HU-48 · Perfil público de un jugador

**Como** usuario, **quiero** ver el perfil público de otro jugador desde el ranking, con su porcentaje de aciertos y su balance, **para** compararme con él sin tener que seguirlo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 31 | 3 (Media) | 1 (it. 8) | Hecha | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-48.1 | Pulso un nombre en el ranking | Se abre su perfil | Veo sus estadísticas pero no sus apuestas. |

### HU-51 · Ligas privadas

**Como** usuario, **quiero** crear una liga privada con mis amigos y unirme con un código de invitación, **para** competir en un ranking solo entre nosotros.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 | 4 (Baja) | 1 (it. 11) | Hecha | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-51.1 | Soy un jugador | Creo una liga | Se genera un código que puedo compartir y yo quedo como miembro. |
| CA-51.2 | Tengo el código de una liga | Lo introduzco | Entro en ella y veo su ranking con los mismos criterios que el ranking general. |
| CA-51.3 | Soy miembro | Salgo de la liga | Dejo de aparecer en su ranking; si soy quien la creó, puedo borrarla. |

### HU-105 · Bloquear a un amigo en un partido o selección

**Como** miembro de una liga, **quiero** pagar 50 monedas para impedir que un compañero apueste en un partido o en una selección concreta, **para** poner una regla temporal entre amigos.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | 5 | Integrada (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-105.1 | Comparto una liga con el amigo al que quiero bloquear y el evento sigue programado | Elijo bloquearle el evento completo o una selección concreta de ese evento | Se aplica el bloqueo solo a ese amigo y se descuentan 50 monedas una sola vez. |
| CA-105.2 | El amigo no comparte mi liga, intento bloquearme a mí mismo o el evento ya empezó, está suspendido o anulado | Solicito el bloqueo | Se rechaza y no se descuentan monedas. |
| CA-105.3 | El amigo ya tenía una apuesta aceptada antes del bloqueo | Se activa el bloqueo | Su apuesta existente no se cancela ni se modifica. |
| CA-105.4 | Ya existe un bloqueo activo para el mismo amigo y alcance | Vuelvo a solicitarlo | No se crea otro bloqueo ni se vuelven a cobrar 50 monedas. |
| CA-105.5 | Mi saldo es inferior a 50 monedas | Confirmo el bloqueo | No se aplica y mi saldo no cambia. |

### HU-106 · Bloquear a toda la liga en un partido o selección

**Como** miembro de una liga, **quiero** pagar 100 monedas para impedir que los demás miembros apuesten en un partido o en una selección concreta, **para** que la regla afecte al grupo entero y no solo a una persona.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | 5 | Integrada (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-106.1 | Soy miembro de una liga y el evento sigue programado | Elijo bloquear el evento completo o una selección concreta para la liga | Se impide apostar en ese alcance a todos los demás miembros de la liga durante la vigencia del bloqueo y se descuentan 100 monedas una sola vez. |
| CA-106.2 | Solicito el bloqueo para toda la liga | Se aplica | Mi propia posibilidad de apostar no queda bloqueada; sí quedan cubiertos los demás miembros mientras pertenezcan a la liga. |
| CA-106.3 | No pertenezco a la liga, el evento ya empezó, está suspendido o anulado | Solicito el bloqueo | Se rechaza sin descontar monedas. |
| CA-106.4 | Ya hay un bloqueo activo para el mismo alcance en esa liga, o mi saldo es inferior a 100 monedas | Intento crearlo | No se crea un bloqueo duplicado ni se descuenta saldo. |

### HU-107 · Aplicar y caducar los bloqueos de la liga

**Como** miembro de una liga, **quiero** que los bloqueos se apliquen de forma visible y temporal, **para** saber qué apuestas están restringidas y cuándo vuelven a estar disponibles.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | 5 | Integrada en parte (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-107.1 | Tengo un bloqueo activo sobre un evento o una selección | Un miembro afectado intenta apostar, también enviando una petición directa | El servidor rechaza la apuesta y explica que está bloqueada por una regla de la liga. |
| CA-107.2 | Se activa un bloqueo | Un miembro afectado abre el partido o su boleto | Ve qué evento o selección está bloqueado y hasta cuándo; no se oculta ni se cambia el resto de las cuotas. |
| CA-107.3 | Había apuestas aceptadas antes del bloqueo | El bloqueo entra en vigor | Se mantienen con sus condiciones originales; el bloqueo solo impide apuestas nuevas dentro del alcance elegido. |
| CA-107.4 | Llega la hora de inicio del evento, o el evento se suspende o anula antes de empezar | El sistema actualiza los bloqueos | El bloqueo caduca y se devuelve el coste pagado. No se puede crear un bloqueo nuevo para un evento empezado, suspendido o anulado. |
| CA-107.5 | El evento empieza a la vez que se solicita un bloqueo, o falla la operación al guardar el bloqueo o cobrar su coste | El sistema procesa la solicitud | El bloqueo y el descuento se confirman juntos o se cancelan juntos; nunca se cobra sin aplicar el bloqueo ni se bloquea sin cobrar. |

### HU-108 · Consultar la clasificación de una liga (propuesta)

**Como** miembro de una liga, **quiero** consultar su clasificación desde un apartado propio, **para** comparar mi posición con la de mis amigos.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | — | Propuesta; por estimar | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-108.1 | Soy miembro de una liga | Abro su apartado «Clasificación» | Veo una tabla con los miembros de esa liga y sus posiciones, separada del ranking general. |
| CA-108.2 | La liga aún no tiene actividad suficiente para comparar a sus miembros | Consulto su clasificación | Se muestra un estado vacío que explica que todavía no hay resultados para clasificar. |
| CA-108.3 | Una liga tiene muchos miembros | Abro su clasificación | Puedo recorrer la lista completa sin que se omitan miembros por el tamaño inicial de la página. |

### HU-109 · Ordenar la clasificación y consultar mi puesto (propuesta)

**Como** miembro de una liga, **quiero** ordenar su clasificación con los mismos criterios que el ranking general y localizar mi puesto, **para** entender cómo se compara mi rendimiento con el del grupo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | — | Propuesta; por estimar | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-109.1 | Estoy viendo la clasificación de una liga | Elijo ganancias, porcentaje de aciertos o saldo | Los miembros se ordenan usando ese criterio, con el mismo cálculo que en el ranking general. |
| CA-109.2 | Cambio el criterio de ordenación | Se actualiza la lista | Se indica qué criterio está activo y el orden corresponde a los valores mostrados. |
| CA-109.3 | Mi puesto queda fuera de los primeros resultados visibles | Consulto la clasificación | Puedo localizar mi posición y ver mi puesto dentro de la liga. |
| CA-109.4 | Dos miembros tienen el mismo valor para el criterio elegido | Se calcula su posición | Se aplica un desempate estable y se muestra el mismo puesto a quienes estén empatados. |

### HU-110 · Mantener la clasificación limitada a los miembros (propuesta)

**Como** miembro de una liga, **quiero** que su clasificación solo esté disponible para sus miembros, **para** comparar resultados dentro del grupo sin hacer públicas las estadísticas de la liga.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | — | Propuesta; por estimar | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-110.1 | No pertenezco a una liga | Intento abrir su clasificación, también mediante una petición directa | No puedo consultar ni obtener sus datos. |
| CA-110.2 | Un usuario deja de pertenecer a una liga | Se consulta su clasificación | Ya no aparece como miembro clasificado de esa liga. |
| CA-110.3 | Soy miembro de una liga y veo su clasificación | Consulto la fila de otra persona | Veo las estadísticas permitidas por el ranking, pero no sus apuestas individuales. |

### HU-111 · Alternar entre mis ligas y la clasificación global

**Como** usuario, **quiero** cambiar entre la clasificación de las ligas a las que pertenezco y la clasificación global desde la misma página, **para** comparar mis grupos con las ligas que se han hecho públicas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | 5 | Integrada (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-111.1 | Abro «Ranking de ligas» | Se carga la página | Puedo elegir entre «Mis ligas» y «Global» sin salir de esa página. |
| CA-111.2 | Elijo «Mis ligas» | Se muestra la clasificación | Solo aparecen las ligas de las que soy miembro, tanto públicas como privadas. |
| CA-111.3 | Elijo «Global» | Se muestra la clasificación | Solo aparecen las ligas públicas, incluidas las mías si son públicas; las privadas quedan excluidas. |
| CA-111.4 | Cambio de una vista a otra o recargo la página | Se vuelve a mostrar la clasificación | La vista activa queda identificada claramente y no se mezclan filas de ambas clasificaciones. |

### HU-112 · Consultar la clasificación global de ligas públicas

**Como** usuario, **quiero** consultar el ranking global de las ligas públicas, **para** descubrir qué grupos reúnen más monedas sin pertenecer a ellos.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | 5 | Integrada (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-112.1 | Consulto la vista «Global» del ranking de ligas | Se calcula el orden | Las ligas públicas se ordenan de mayor a menor por la suma de los saldos de sus miembros activos. |
| CA-112.2 | Dos ligas tienen el mismo saldo total | Se genera la clasificación | El desempate es estable y la posición coincide con el orden mostrado. |
| CA-112.3 | Una liga no tiene miembros activos o no hay ligas públicas | Abro la vista global | Se muestra un estado vacío comprensible y no se incluyen ligas privadas. |
| CA-112.4 | No pertenezco a una liga pública que aparece en el ranking | Consulto su fila global | Solo veo los datos públicos de la liga —nombre, número de miembros activos y saldo total—, nunca los saldos individuales ni las apuestas de sus miembros. |

### HU-113 · Cambiar la visibilidad de una liga

**Como** creador de una liga, **quiero** cambiarla entre pública y privada, **para** decidir si aparece en la clasificación global sin perder la clasificación interna de sus miembros.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 33 (ampliación de HU-51) | 3 (Media) | 5 | Integrada (Sprint 5) | — | — | — | — | — |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-113.1 | Creo una liga | Se guarda por primera vez | Es privada de forma predeterminada y no aparece en la clasificación global. |
| CA-113.2 | Soy quien creó la liga | Cambio su visibilidad a pública | La liga aparece en la clasificación global y se muestran únicamente sus datos públicos agregados. |
| CA-113.3 | Soy quien creó una liga pública | Cambio su visibilidad a privada | Deja de aparecer en la clasificación global y sigue apareciendo en «Mis ligas» para sus miembros. |
| CA-113.4 | No soy quien creó la liga, o no pertenezco a ella | Intento cambiar su visibilidad, también mediante una petición directa | El servidor rechaza la operación y conserva la visibilidad anterior. |
| CA-113.5 | Cambio la visibilidad de la liga | Se actualizan las clasificaciones | No cambian los miembros, los retos ni los bloqueos de la liga; solo cambia su inclusión en la vista global y el acceso a sus datos públicos agregados. |

---

## Apuestas a largo plazo

### HU-44 · Apuestas a largo plazo

**Como** usuario, **quiero** apostar a quién ganará un premio o una competición (Balón de Oro, campeón de LaLiga, Pichichi…), **para** predecir resultados a largo plazo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 28 | 3 (Media) | 1 (it. 6) | Hecha | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-44.1 | Hay un mercado abierto | Entro en él | Veo todos los candidatos con su cuota. |
| CA-44.2 | Elijo un candidato y un importe con saldo suficiente | Confirmo | Se descuenta el importe y la apuesta queda activa. |
| CA-44.3 | El mercado está cerrado | Intento apostar | El sistema no lo permite. |

### HU-45 · Gestión de mercados

**Como** creador de apuestas, **quiero** crear un mercado a largo plazo con sus candidatos y cuotas, cerrarlo y marcar el ganador, **para** gestionar este tipo de apuestas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 28 | 3 (Media) | 1 (it. 6) | Hecha | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-45.1 | Creo un mercado con al menos dos candidatos y sus cuotas | Lo guardo | Aparece en el catálogo. |
| CA-45.2 | Un mercado abierto | Lo cierro | Deja de admitir apuestas. |
| CA-45.3 | Un mercado con apuestas | Marco el candidato ganador | Las apuestas a ese candidato se cobran y el resto pasan a perdidas. |

---

## Casino

Juegos de azar con moneditas virtuales. El azar lo decide siempre el servidor; cada partida deja sus movimientos de saldo (HU-55) y cuenta en el juego responsable (HU-10).

### HU-56 · Ruleta europea

**Como** usuario, **quiero** apostar moneditas a un número, un color, par o impar, o una docena en una ruleta europea, **para** jugar con mi saldo virtual.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | 4 (Baja) | 3 | Integrada (Sprint 5) | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-56.1 | Tengo saldo y un importe dentro de los límites del casino | Apuesto y giro | El servidor elige el número (0–36), se cobra el importe y, si acierto, se paga: pleno 35 a 1, color o par/impar 1 a 1 y docena 2 a 1. |
| CA-56.2 | Sale el 0 | Tenía una apuesta a color, par/impar o docena | La pierdo. |
| CA-56.3 | No tengo saldo, el importe está fuera de los límites, estoy en pausa o supero mis límites (HU-10) | Intento girar | No se juega, no se cobra nada y se me avisa. |

### HU-57 · Blackjack

**Como** usuario, **quiero** jugar al blackjack contra la banca pudiendo pedir, plantarme y doblar, **para** jugar con mi saldo virtual.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | 4 (Baja) | 3 | Integrada (Sprint 5) | 8 | 656 | 47 | 7,6 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-57.1 | Empiezo una mano con un importe válido | Se reparte | Se cobra el importe y veo mis dos cartas y solo una de la banca; la otra no llega al navegador. |
| CA-57.2 | Mi mano es blackjack y la de la banca no | Termina la mano | Cobro el importe más 3 a 2 (o lo que diga la configuración). |
| CA-57.3 | Tengo dos cartas y saldo para otra apuesta igual | Doblo | Se cobra otro importe, recibo una sola carta y la mano termina. |
| CA-57.4 | Juego mi mano | Paso de 21 o me planto | Si me paso, pierdo. Si me planto, la banca juega con sus reglas y cobro el doble si gano o recupero el importe si empato. |
| CA-57.5 | Tengo una mano a medias | Recargo la página o pulso dos veces | Sigo en la misma mano: no se reparte otra ni se cobra dos veces. |

Sin dividir parejas ni seguro (fuera del alcance).

### HU-58 · Historial de partidas del casino

**Como** usuario, **quiero** ver mis partidas del casino, **para** saber cuánto he jugado y ganado.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | 4 (Baja) | 3 | Integrada (Sprint 5) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-58.1 | He jugado partidas | Abro el historial | Veo juego, fecha, apuesta, resultado, importe y balance, de la más reciente a la más antigua. |

### HU-59 · Configurar el casino

**Como** creador de apuestas, **quiero** fijar los límites por apuesta y las reglas del casino, **para** controlar la ventaja de la casa.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | 4 (Baja) | 3 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-59.1 | Estoy en *Gestión → Casino* | Guardo el mínimo, el máximo y las reglas del blackjack (barajas, si la banca pide con 17 blando y si el blackjack paga 3:2 o 6:5) | Se aplican a las partidas nuevas y veo la ventaja teórica de cada juego. |
| CA-59.2 | Cierro el casino | Un usuario intenta jugar | No puede. |

La ventaja de la casa se controla con reglas reales: la ruleta europea siempre tiene un 2,70 % (un 0 entre 37 números); la del blackjack es aproximada, con estrategia básica.

### HU-74 · Baccarat

**Como** usuario, **quiero** jugar al baccarat Punto Banco en el casino, **para** tener un juego de cartas en el que no hay que decidir nada después de apostar.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 37 | 4 (Baja) | 4 | Integrada (Sprint 5) | 5 | 410 | 29 | 5,0 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-74.1 | Tengo saldo y elijo Jugador, Banca o Empate con un importe | Reparto la mano | Se cobra el importe, las cartas se reparten solas y se paga: Jugador 1 a 1, Banca 1 a 1 menos un 5 % de comisión y Empate 8 a 1. La mano queda en el historial del casino. |
| CA-74.2 | No elijo a qué apuesto | Intento repartir | Se me pide elegir y no se cobra nada. |
| CA-74.3 | La mano acaba en empate | Había apostado a Jugador o a Banca | Se me devuelve lo apostado. |
| CA-74.4 | Las cartas de una mano | Se juega | Se siguen las reglas del Punto Banco: figuras y dieces valen 0 y solo cuentan las unidades; con 8 o 9 nadie pide; el jugador pide con 5 o menos y se planta con 6 o 7; la banca sigue la tabla de la tercera carta. |

Se juega con 8 barajas y va junto al blackjack (HU-57), en la misma página del casino.

---

## Anuncios internos

### HU-60 · Anuncios de otras secciones

**Como** visitante o usuario, **quiero** ver anuncios de otras secciones de la web y pulsarlos, **para** descubrir lo que ofrece.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 38 | 4 (Baja) | 3 | Integrada (Sprint 5) | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-60.1 | Hay anuncios activos para la sección de la página | La abro | Veo uno de los de prioridad más alta; si hay varios con esa prioridad, se turnan al azar. |
| CA-60.2 | Estoy en una sección | Se elige el anuncio | Nunca es uno que lleve a esa misma sección. |
| CA-60.3 | Veo un anuncio | Lo pulso | Voy directo a su sección. Los que llevan a secciones que necesitan sesión (ligas, Mi cuenta) no se enseñan a los visitantes. |

### HU-61 · Gestionar los anuncios

**Como** creador de apuestas, **quiero** crear, editar, activar y desactivar anuncios con su texto, su destino, las secciones donde salen, su prioridad y sus fechas, **para** decidir qué se promociona sin tocar el código.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 38 | 4 (Baja) | 3 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-61.1 | Creo un anuncio con un destino de la web y al menos una sección | Lo guardo | Sale en esas secciones mientras esté activo y dentro de sus fechas. |
| CA-61.2 | El destino es una dirección de otra web o no es una sección conocida | Lo guardo | Se rechaza con un mensaje. |

### HU-62 · Medir los clics de cada anuncio

**Como** creador de apuestas, **quiero** ver cuántas veces se ha mostrado y pulsado cada anuncio, **para** saber cuáles funcionan.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 38 | 4 (Baja) | 3 | Integrada (Sprint 5) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-62.1 | Un usuario o un visitante pulsa un anuncio | Lo abro en Gestión | Queda apuntado el clic con su fecha y la página de origen, y veo las impresiones, los clics y el porcentaje de clics de cada anuncio. |

---

## Mercados de predicción

Preguntas de sí o no, tipo Polymarket. El precio de cada respuesta va de 0 a 1 y se lee como probabilidad; lo calcula un creador de mercado automático (LMSR) con cada compra y venta. No son los mercados a largo plazo de HU-44 (cuotas fijas del creador).

### HU-63 · Crear, cerrar y anular mercados de predicción

**Como** creador de apuestas, **quiero** crear un mercado de sí o no con su pregunta, su fecha de cierre y su liquidez, y poder cerrarlo o anularlo, **para** ofrecer predicciones.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | 4 (Baja) | 3 | Integrada (06/11) | 3 | 246 | 18 | 3,0 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-63.1 | Escribo la pregunta, el cierre, la liquidez y la comisión | Creo el mercado | Queda abierto con el «Sí» y el «No» a 0,50. |
| CA-63.2 | Un mercado abierto | Lo cierro o llega su fecha | Ya no admite compras ni ventas. |
| CA-63.3 | Un mercado con posiciones | Lo anulo | A cada usuario se le devuelve lo que pagó menos lo que recuperó vendiendo. |

### HU-64 · Comprar participaciones

**Como** usuario, **quiero** comprar participaciones del «Sí» o del «No» con mi saldo, **para** ganar si acierto la predicción.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | 4 (Baja) | 3 | Integrada (06/11) | 8 | 656 | 47 | 7,5 | 7,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-64.1 | Elijo un lado y un importe | Pido la previsión y confirmo | Antes de confirmar veo cuántas participaciones recibo y el precio medio; al confirmar se cobra el importe (comisión incluida) y el precio de ese lado sube. |
| CA-64.2 | No tengo saldo, el importe está fuera de los límites, el mercado está cerrado o resuelto, o el juego responsable no lo permite | Intento comprar | No se compra y se me avisa. |
| CA-64.3 | El precio cambia entre que lo veo y confirmo | Confirmo | No se compra y se me pide aceptar el precio nuevo, como en HU-29. |

### HU-65 · Vender participaciones

**Como** usuario, **quiero** vender mis participaciones antes del cierre, **para** recoger ganancias o cortar pérdidas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | 4 (Baja) | 3 | Integrada (06/11) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-65.1 | Tengo participaciones de un mercado abierto | Vendo | Recibo su valor menos la comisión y el precio de ese lado baja. |
| CA-65.2 | Quiero vender más participaciones de las que tengo | Lo intento | No se vende nada y se me avisa. |

### HU-66 · Resolver el mercado y pagar

**Como** creador de apuestas, **quiero** marcar si la respuesta fue «Sí» o «No», **para** pagar a los ganadores.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | 4 (Baja) | 3 | Integrada (06/11) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-66.1 | Un mercado con posiciones | Marco la respuesta | Cada participación ganadora paga 1 monedita y las perdedoras nada, cada pago con su movimiento de saldo y su aviso (HU-37). |
| CA-66.2 | Un mercado ya resuelto | Corrijo la respuesta | Los saldos se ajustan por la diferencia con lo ya pagado (ADR 0002). |

### HU-67 · Estadísticas y evolución del precio

**Como** visitante, **quiero** ver el precio actual, el volumen, el número de participantes y la evolución del precio de cada mercado, **para** saber qué piensa la comunidad.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | 4 (Baja) | 3 | Integrada (06/11) | 3 | 246 | 18 | 2,7 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-67.1 | Un mercado con operaciones | Lo abro, aunque no haya iniciado sesión | Veo el precio, el volumen, los participantes y un gráfico con el precio del «Sí» después de cada operación. |

### HU-68 · Posiciones de mis compañeros de liga

**Como** usuario, **quiero** ver en cada mercado qué posiciones tienen los miembros de mis ligas privadas, **para** comparar mis predicciones con las de mis amigos.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 39 | 4 (Baja) | 3 | Integrada (06/11) | 2 | 164 | 12 | 2,1 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-68.1 | Comparto una liga privada (HU-51) con otros jugadores | Abro un mercado | Veo el lado, las participaciones y el valor actual de los que tienen posición. |
| CA-68.2 | Alguien no comparte ninguna liga conmigo | Abre el mercado | No ve mis posiciones (tampoco se enseñan en el perfil público, HU-48). |

---

## Cartera de criptomonedas

Criptomonedas reales a su precio real en euros, compradas y vendidas con moneditas (1 monedita = 1 €). Solo el precio es real: nadie tiene criptomonedas de verdad, no hay carteras externas ni blockchain, y todo sigue siendo saldo virtual. Las protagonistas son las memecoins (DOGE, SHIB, PEPE, WIF, BONK, FLOKI, FARTCOIN), las de más riesgo y las que más se mueven; BTC, ETH y SOL quedan como referencia. Los precios vienen de la API pública de CoinGecko.

### HU-69 · Ver criptomonedas a su precio real

**Como** visitante, **quiero** ver las criptomonedas con su precio real en euros, cuánto se han movido en 24 horas y su gráfico de la semana, **para** decidir en cuál me la juego.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | 4 (Baja) | 3 | Integrada (06/11) | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-69.1 | Hay criptomonedas con precio | Abro *Cripto*, aunque no haya iniciado sesión | Veo primero las memecoins y después las de referencia, cada una con su precio (con las cifras necesarias aunque valga una fracción de céntimo) y su variación en 24 horas, y destacada la que más se ha movido. |
| CA-69.2 | Una cripto con precios guardados | Abro su ficha | Veo un gráfico de su precio en los últimos 7 días. |
| CA-69.3 | Cualquier página de criptomonedas | La abro | Veo un aviso de riesgo (muy alto en las memecoins) y que solo se juega con moneditas. |
| CA-69.4 | La aplicación está en marcha | Pasan 2 minutos | Los precios se descargan solos de CoinGecko; si la API falla, la web sigue funcionando y se reintenta después. |

### HU-70 · Comprar criptomonedas con moneditas

**Como** usuario, **quiero** comprar una criptomoneda con mi saldo al precio actual, **para** ganar si sube.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | 4 (Baja) | 3 | Integrada (06/11) | 5 | 410 | 29 | 4,9 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-70.1 | Elijo una cripto y un importe | Pido la previsión y confirmo | Antes de confirmar veo cuánta cripto recibo y la comisión; al confirmar se cobra el importe (comisión incluida) con un movimiento de saldo (HU-55). |
| CA-70.2 | No tengo saldo, el importe está fuera de los límites (HU-07), soy el creador o el juego responsable no lo permite (HU-10) | Intento comprar | No se compra, no se cobra y se me avisa. |
| CA-70.3 | Llega un precio nuevo entre que veo la previsión y confirmo | Confirmo | No se compra y se me enseña el precio nuevo para que decida, como en HU-29. |
| CA-70.4 | El último precio tiene más de 15 minutos o no hay | Intento comprar | No se compra: hace falta un precio al día. |

### HU-71 · Vender criptomonedas

**Como** usuario, **quiero** vender una parte o todo lo que tengo de una criptomoneda, **para** recoger ganancias o cortar pérdidas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | 4 (Baja) | 3 | Integrada (06/11) | 3 | 246 | 18 | 2,8 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-71.1 | Tengo una cripto con precio al día | Vendo una parte o todo | Recibo cantidad × precio menos la comisión, y el coste de lo que me queda baja en la misma proporción. |
| CA-71.2 | Quiero vender más de lo que tengo, o algo que no tengo | Lo intento | No se vende nada y se me avisa. |

### HU-72 · Mi cartera de criptomonedas

**Como** usuario, **quiero** ver lo que tengo de cada criptomoneda, cuánto vale y cuánto gano o pierdo, **para** seguir mis inversiones.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | 4 (Baja) | 3 | Integrada (06/11) | 2 | 164 | 12 | 2,0 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-72.1 | Tengo criptomonedas | Abro *Mi cartera* | Veo de cada una la cantidad, el precio, el valor, lo que me costó y la ganancia o pérdida en moneditas y en %, y los totales. |
| CA-72.2 | He comprado y vendido | Abro *Mi cartera* | Veo mis últimas operaciones con su precio, importe y comisión. |

### HU-73 · Configurar las criptomonedas

**Como** creador de apuestas, **quiero** abrir o cerrar la sección, fijar la comisión y elegir qué criptos se ofrecen, **para** controlar lo que gana o arriesga la casa.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 40 | 4 (Baja) | 3 | Integrada (06/11) | 3 | 246 | 18 | 2,7 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-73.1 | Estoy en *Gestión → Criptomonedas* | Guardo si está abierta, la comisión (0–10 %) y los minutos que sirve un precio (1–120) | Se aplica a las compras y ventas siguientes; con la sección cerrada se ven los precios pero nadie opera. |
| CA-73.2 | Retiro una cripto | Un usuario la busca | Ya no aparece ni se puede comprar, pero quien la tenga puede venderla. |
| CA-73.3 | Quiero precios ya | Pulso *Actualizar precios ahora* | Se descargan en el momento; si CoinGecko no responde, se me avisa. |

## Quiniela

Varios partidos con 1, X o 2 y un precio por boleto. Sin cuotas: el bote sale de lo que pagan los jugadores y se reparte entre los que más aciertan; la casa solo se queda la comisión. Es la propuesta de la [discusión #74](https://github.com/riverri/IS1/discussions/74).

### HU-78 · Montar una quiniela

**Como** creador de apuestas, **quiero** montar una quiniela con varios partidos y un precio por boleto, **para** que los jugadores compitan entre ellos sin jugar contra la casa.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 45 | 4 (Baja) | 4 | Integrada (16/11) | 3 | 246 | 18 | 3,1 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-78.1 | Hay partidos programados de deportes con empate | Elijo de 3 a 15 y el precio del boleto | Se crea la quiniela con los partidos en orden de fecha. |
| CA-78.2 | Elijo menos de 3 partidos | Intento crearla | No se crea y se dice por qué. |
| CA-78.3 | Soy un usuario sin rol de creador | Intento entrar en la gestión de quinielas | No puedo. |

No tiene cuotas: el bote sale de lo que pagan los jugadores y la casa solo se queda un 10 % de comisión. Un evento que está en una quiniela no se puede borrar ni cambiar de equipos; si está mal, se anula.

**Pruebas:** `QuinielaWebTest` (CA-78.1 a CA-78.3).

### HU-79 · Echar un boleto

**Como** usuario, **quiero** rellenar un boleto con 1, X o 2 en cada partido de la quiniela, **para** optar al bote.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 45 | 4 (Baja) | 4 | Integrada (16/11) | 3 | 246 | 18 | 3,1 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-79.1 | Una quiniela abierta y saldo suficiente | Echo mi boleto con un pronóstico en cada partido | Se cobra el precio y veo mi boleto en la página. Solo puedo echar uno. |
| CA-79.2 | Ha empezado un partido, o alguno está suspendido o anulado | Intento echar un boleto | La quiniela está cerrada y no se admite. |
| CA-79.3 | Soy el creador de apuestas | Abro la quiniela | No puedo jugar. |

El boleto cuenta para los límites del juego responsable (HU-10), como una apuesta.

**Pruebas:** `QuinielaWebTest` (CA-79.1 a CA-79.3).

### HU-80 · Repartir el bote de la quiniela

**Como** creador de apuestas, **quiero** repartir el bote cuando se han jugado todos los partidos, **para** pagar a quienes más aciertan.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 45 | 4 (Baja) | 4 | Integrada (16/11) | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-80.1 | Se han jugado todos los partidos | Reparto el bote | El bote (lo pagado menos el 10 % de la casa) se reparte a partes iguales entre los boletos con más aciertos, y cada jugador recibe un aviso. |
| CA-80.2 | Se ha anulado un partido | Se cuentan los aciertos | Ese partido cuenta como acierto para todos. |
| CA-80.3 | Nadie ha acertado ninguno, o anulo la quiniela antes de repartirla | Se reparte o se anula | Se devuelve el precio de todos los boletos. |

El bote y cada parte se redondean hacia abajo; los céntimos que no se pueden repartir se quedan en la casa.

**Pruebas:** `QuinielaWebTest` (CA-80.1 a CA-80.3).

## Fantasy

Cada usuario se hace un equipo con jugadores reales de las plantillas (HU-49 y HU-50) y suma puntos según cómo jueguen.

### HU-81 · Crear un equipo fantasy y fichar

**Como** usuario, **quiero** montar un equipo de siete futbolistas reales con un presupuesto, **para** competir con lo que sé de fútbol sin gastar moneditas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 46 | 4 (Baja) | 4 | Integrada (Sprint 5) | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-81.1 | No tengo equipo | Entro en *Fantasy* | Se me pide crearlo, y después ficho con un presupuesto de 90 millones ficticios. |
| CA-81.2 | Tengo equipo | Intento fichar a un jugador | Solo se puede si hay hueco en su posición (1 portero, 2 defensas, 2 centrocampistas y 2 delanteros), como mucho 2 del mismo club y sin pasar del presupuesto. El mercado filtra por posición y dice por qué no se puede fichar a alguien. |
| CA-81.3 | Soy el creador de apuestas | Entro en *Fantasy* | No puedo crear un equipo. |

El precio de cada jugador sale de su nota y de la calidad de su equipo (de unos 7 a unos 20 millones). Al vender a un jugador se recupera lo que costó.

### HU-82 · Puntos de los jugadores fantasy

**Como** usuario, **quiero** que mis jugadores sumen puntos con los partidos de sus clubes, **para** seguir cómo va mi equipo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 46 | 4 (Baja) | 4 | Integrada (Sprint 5) | 5 | 410 | 29 | 4,7 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-82.1 | Ficho a un jugador | Se juegan partidos de su club | Solo le cuentan los que empiezan después de ficharlo. Si lo vendo, lo que ya sumó se queda. |
| CA-82.2 | Un partido de su club ha terminado | Se calculan sus puntos | Victoria 3 y empate 1. Portería a cero: portero +4, defensa +3 y centrocampista +1. Cada gol de su club: delantero +2 y centrocampista +1. Si su club encaja 3 o más, portero y defensa −1. Sin marcador, solo cuentan la victoria y el empate. |
| CA-82.3 | La jornada (de lunes a domingo) ya ha empezado: se ha jugado o está en juego su primer partido | Ficho o vendo a un jugador | El cambio cuenta desde la jornada siguiente: el fichado suma desde el lunes y el vendido sigue sumando lo que queda de jornada. Antes del primer partido, cuenta enseguida ([ADR 0007](../decisiones/0007-jornada-fantasy.md)). |

No hay estadísticas de cada jugador (goles, tarjetas…): todo sale del resultado de su club y de su posición.

### HU-83 · Clasificación fantasy

**Como** usuario, **quiero** ver la clasificación de todos los equipos fantasy, **para** saber cómo voy frente a los demás.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 46 | 4 (Baja) | 4 | Integrada (Sprint 5) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-83.1 | Hay varios equipos | Abro la clasificación | Salen de más a menos puntos; con los mismos puntos va delante el que se creó antes. Si el creador corrige un resultado, la clasificación se corrige sola. |

## Seguimiento de equipos y cuotas

Seguir a unos equipos y vigilar las cuotas sin tener que entrar a mirarlas.

### HU-86 · Equipos favoritos

**Como** usuario, **quiero** marcar equipos como favoritos, **para** ver sus partidos los primeros y enterarme cuando se publique uno nuevo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 48 | 4 (Baja) | 5 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-86.1 | Estoy en la ficha de un equipo | Pulso «Añadir a favoritos» | Queda en mis favoritos; si lo vuelvo a pulsar, se quita. El creador de apuestas no sigue equipos. |
| CA-86.2 | Tengo equipos favoritos | Abro el catálogo sin filtros ni búsqueda | Sus próximos partidos salen arriba, en «Tus equipos». |
| CA-86.3 | Sigo a un equipo | Se publica un partido suyo, a mano o desde la API | Me llega un aviso con el partido y la fecha, uno solo aunque siga a los dos equipos. |

**Pruebas:** `SeguimientoWebTest`.

### HU-90 · Alerta de cuota

**Como** usuario, **quiero** que me avisen cuando la cuota de un resultado llegue al valor que elija, **para** apostar en ese momento.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 48 | 4 (Baja) | 5 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-90.1 | Un partido que admite apuestas | Pongo una alerta con un resultado y una cuota mayor que la actual | Se guarda (hasta 3 por partido); con una cuota igual o menor que la actual, no. |
| CA-90.2 | Tengo una alerta | La cuota llega a ese valor | Me llega un aviso, una sola vez. |
| CA-90.3 | Tengo una alerta | Empieza el partido sin que llegue | La alerta se borra. |

Se comprueban cada vez que cambian las cuotas del partido y, por si cambian por otro motivo, cada 10 minutos.

**Pruebas:** `SeguimientoWebTest`.

### HU-91 · Evolución de la cuota

**Como** usuario, **quiero** ver cómo ha cambiado la cuota de cada resultado, **para** decidir cuándo apostar.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 48 | 4 (Baja) | 5 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-91.1 | Las cuotas de un partido han cambiado | Abro el partido | Veo un gráfico con una línea por resultado y de cuánto a cuánto ha ido cada una. |
| CA-91.2 | Se apuesta, se cancela o se cambia un importe | Cambian las cuotas | Queda apuntado el valor nuevo; si no cambian, no se repite. |

**Pruebas:** `EvolucionCuotaWebTest`.

### HU-93 · Publicidad de Better en otras webs

**Como** creador de apuestas, **quiero** saber cuántas visitas y cuántas cuentas nuevas trae cada enlace o banner de Better publicado fuera de la web, **para** saber qué publicidad funciona y atraer a más jugadores.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 58 | 2 (Alta) | - | Por hacer (Sprint 5-6) | 2 | - | - | - | - |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-93.1 | Soy el creador | Doy de alta en Gestión una campaña con su nombre (por ejemplo, «Instagram») | Obtengo el enlace a Better de esa campaña, para ponerlo en el banner o en la publicación de la otra web. |
| CA-93.2 | Hay una campaña dada de alta | Un visitante entra por su enlace | Ve la portada de siempre, con el botón para crear una cuenta, y la campaña suma una visita. Si crea su cuenta en esa misma visita, la campaña suma también una cuenta nueva. Un enlace con una campaña que no existe cuenta como una visita normal. |
| CA-93.3 | Soy el creador | Abro las campañas en Gestión | Veo de cada campaña sus visitas, sus cuentas nuevas y qué parte de las visitas acaba creando una cuenta. |

El banner o la publicación en sí están en la otra web, fuera de la aplicación: Better solo decide adónde lleva el enlace y mide lo que pasa al llegar. Los anuncios dentro de la web son HU-60 a HU-62 (fila 38 del backlog), y la portada ya presenta Better a quien llega (HU-08).

*Redactada de nuevo el 27/11* a partir de la versión del 20/11: el rol era el Product Owner, que no usa la web; el único criterio («me muestra un overview de la app») no se podía comprobar, y el banner externo queda fuera del sistema. Se mantiene la idea: dar a conocer Better y que crezca el número de jugadores. Falta estimarla.

**Pruebas:** por hacer cuando se implemente.

### HU-96 · Estadísticas por competición

**Como** usuario, **quiero** ver mis aciertos y mi balance en cada competición, **para** saber en qué acierto más.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 56 | 4 (Baja) | 5 | Integrada (Sprint 5) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-96.1 | Tengo apuestas resueltas | Abro *Mis apuestas* | Veo, por cada competición, cuántas, el porcentaje de aciertos y el balance. Las combinadas y las de largo plazo van aparte. |
| CA-96.2 | Una apuesta sigue activa | Abro *Mis apuestas* | No cuenta. |

**Pruebas:** `EstadisticasCompeticionWebTest`.

---

## Accesibilidad

Tener una discapacidad o usar la web de otra forma no quita el derecho a apostar: cualquiera tiene que poder usarla entera. La referencia es el nivel AA de las pautas WCAG 2.1, el que pide la norma europea EN 301 549.

**Pruebas:** `AccesibilidadWebTest` y `AccesibilidadCssTest`, y en cada pull request la comprobación con navegador de [`a11y/comprobar.js`](../../a11y/comprobar.js): las reglas de WCAG 2.1 A y AA de axe en las páginas principales, con colores claros y oscuros, el uso con el teclado y la página a 320 px y ampliada al 200 %.

### HU-97 · Usar la web solo con el teclado

**Como** usuario que no puede usar el ratón, **quiero** recorrer y usar toda la web con el teclado, **para** apostar igual que los demás.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 57 | 3 (Media) | 5 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-97.1 | Cualquier página | Recorro la página con el tabulador | Llego a todos los enlaces, botones y campos en el orden en que se ven, y siempre se ve dónde está el foco. |
| CA-97.2 | Estoy en un partido | Elijo un resultado con las flechas o la barra espaciadora y escribo el importe | Puedo confirmar la apuesta o añadirla al boleto sin usar el ratón. |
| CA-97.3 | Acabo de entrar en una página | Pulso el tabulador por primera vez | Aparece un enlace «Saltar al contenido» que me lleva directo al contenido, sin pasar por todo el menú. |

---

### HU-98 · Usar la web con un lector de pantalla

**Como** usuario ciego que usa un lector de pantalla, **quiero** que cada cuota, botón e icono tenga un nombre que se entienda, **para** saber qué estoy apostando.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 57 | 3 (Media) | 5 | Integrada (Sprint 5) | 5 | 410 | 29 | 4,8 | 4,8 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-98.1 | Estoy en un partido o en el catálogo | El lector lee una cuota | Dice a qué resultado es y cuánto paga, por ejemplo «Getafe CF gana, cuota 2,72», no solo «2,72». |
| CA-98.2 | Una página con iconos o emojis (campana, luna, deportes) | El lector llega a ellos | Lee lo que significan («Avisos», «Modo oscuro», «Fútbol») o se los salta si son decoración. |
| CA-98.3 | Hago algo y la web responde con un mensaje (apuesta hecha, cuota cambiada, error) | Aparece el mensaje | El lector lo anuncia sin que tenga que buscarlo. |
| CA-98.4 | Un formulario con un error | Lo envío mal | El error está junto a su campo y el lector lo lee al llegar al campo. |

---

### HU-99 · Leer bien con poca visión

**Como** usuario con baja visión, **quiero** textos y botones con buen contraste y poder ampliar la página, **para** leer las cuotas sin esfuerzo.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 57 | 3 (Media) | 5 | Integrada (Sprint 5) | 3 | 246 | 18 | 2,9 | 2,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-99.1 | Cualquier página, con colores claros u oscuros | Miro un texto o un botón | El texto tiene un contraste de al menos 4,5:1 con su fondo (3:1 si es grande), y los bordes de botones y campos, de al menos 3:1. |
| CA-99.2 | Cualquier página | La amplío al 200 % en el navegador | Se sigue leyendo todo y se puede usar, sin que nada se corte ni se monte. |
| CA-99.3 | Un móvil de 320 px de ancho | Abro cualquier página | No hace falta desplazarse a los lados para leer el contenido. |

---

### HU-100 · No depender solo del color

**Como** usuario daltónico, **quiero** que nada se distinga solo por el color, **para** entender igual los resultados y las cuotas.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 57 | 3 (Media) | 5 | Integrada (Sprint 5) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-100.1 | Mi historial de apuestas | Miro si una apuesta se ganó o se perdió | Lo dice con texto o con un símbolo, además del color. |
| CA-100.2 | La evolución de la cuota o el gráfico de una criptomoneda | Comparo las líneas o veo si sube o baja | Cada línea se distingue también por su forma o su etiqueta, y las subidas y bajadas llevan signo (+ o −). |
| CA-100.3 | Un formulario con un error | Miro el campo que está mal | El error se indica con texto, no solo con el borde rojo. |

---

### HU-101 · Menos movimiento

**Como** usuario al que le marean o le molestan las animaciones, **quiero** que la web respete la opción de reducir el movimiento de mi dispositivo, **para** usarla sin molestias.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 57 | 3 (Media) | 5 | Integrada (Sprint 5) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-101.1 | Tengo activada la opción «reducir movimiento» del sistema | Uso la web | No hay animaciones ni transiciones de movimiento: los cambios se ven de golpe. |
| CA-101.2 | Cualquier página | La abro | Nada parpadea más de tres veces por segundo ni se mueve solo durante más de cinco segundos. |

### HU-114 · Exportar mi historial

**Como** usuario, **quiero** descargar mis apuestas y mis movimientos en CSV, **para** tener mis datos fuera de la web.

| Fila del backlog | Prioridad | Sprint | Estado | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---|---|---|---:|---:|---:|---:|---:|
| 53 | 4 (Baja) | 5 | Integrada (Sprint 5) | 2 | 164 | 12 | 1,9 | 1,9 $ |

**Criterios de aceptación**

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-114.1 | Tengo apuestas | Pulso «Descargar» en *Mis apuestas* | Me baja un CSV con todas: fecha, apuesta, importe, cuota, estado y cobrado. Se abre con Excel (punto y coma y coma decimal). |
| CA-114.2 | Estoy en *Mi cuenta* | Descargo mis movimientos | Me baja un CSV con cada cambio del saldo, su motivo y el saldo que dejó. Sin sesión no se descarga nada. |

**Pruebas:** `ExportacionWebTest`.

---

## Pendientes de refinar

Del bloque "Apostador" del PDF, sin criterios de aceptación:
- *Como apostador quiero poder introducir dinero para apostar.* Choca con "sin dinero real". Seguramente se refiere a la recarga de moneditas (HU-14).
- *Como apostador quiero poder consultar las apuestas para decidir en qué apostar.* Queda cubierta por HU-08, HU-19 y HU-20.
