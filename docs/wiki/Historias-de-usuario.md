# Historias de usuario

El documento completo, con la ficha y los criterios de aceptación de cada historia, está en [`docs/requisitos/historias-de-usuario.md`](https://github.com/riverri/IS1/blob/main/docs/requisitos/historias-de-usuario.md).

## Formato
- **Historia:** *Como [actor], quiero [acción], para [objetivo]*.
- **Criterios de aceptación:** cada uno con un identificador **CA-xx.y** (historia xx, criterio y) y en tres partes: **Dado** (situación de partida), **Cuando** (la acción) y **Entonces** (el resultado esperado).

Ejemplo:

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| CA-05.1 | Un evento programado | Lo suspendo | Deja de admitir apuestas nuevas hasta que lo reactive |
| CA-05.2 | Un evento con apuestas | Lo anulo | Se devuelve a cada usuario el importe que apostó en él |

La [matriz de trazabilidad](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md) usa los mismos identificadores para enlazar cada criterio con su prueba. Cómo cumple cada historia el criterio INVEST está en [invest.md](https://github.com/riverri/IS1/blob/main/docs/requisitos/invest.md).

## Estado

| Estado | Historias |
|---|---|
| **Hechas (46)** | HU-01 a HU-14, HU-17 a HU-37, HU-44 a HU-54 |
| **Integradas (56)** | Del Sprint 4: HU-63 a HU-68 (mercados de predicción) y HU-69 a HU-73 (cartera de criptomonedas) el 06/11; HU-85 (tiempo de carga) el 15/11; HU-76 (riesgo de la casa), HU-77 (correo al ganar), HU-78 a HU-80 (quiniela) y HU-84 (ranking por tipo de juego) el 16/11. Del Sprint 5: HU-75 (cash-out), HU-86 a HU-92 y HU-94 a HU-96 (favoritos, supercuota, logros, retos, alertas y evolución de la cuota, resumen semanal, modo oscuro, beneficio diario y estadísticas por competición), HU-97 a HU-101 (accesibilidad) y HU-114 (exportar el historial); el 20/11, HU-102 a HU-107 (cash-out de combinadas y bloqueos de liga; HU-107 en parte) y HU-111 a HU-113 (ranking de ligas públicas); el 21/11, HU-55 (movimientos del saldo), HU-56 a HU-59 y HU-74 (casino), HU-60 a HU-62 (anuncios internos) y HU-81 a HU-83 (liga fantasy). Están en `main`; falta que el Product Owner las acepte en la review de su sprint |
| **Por hacer (1)** | HU-93 (publicidad de Better en otras webs: visitas y cuentas nuevas de cada campaña) |
| **Propuestas (3)** | HU-108 a HU-110 (clasificación de una liga): casi todo lo que piden ya lo da el ranking de cada liga (HU-51) |
| **Descartadas (8)** | HU-15 y HU-16 (cuenta bancaria y retiradas: chocan con "sin dinero real") y HU-38 a HU-43 (seguir usuarios, feed, privacidad, copiar apuestas). En su lugar se hicieron el perfil público (HU-48) y las ligas (HU-51) |

En total hay **114 historias y 285 criterios de aceptación**: 98 son de historias hechas, 161 de las integradas, 3 de la que está por hacer, 10 de las propuestas y 13 de las descartadas.

## Estimación

Cada historia tiene su **tamaño** (puntos de historia y líneas de código), las **horas sin IA y con IA** y el **coste de la IA**. El método completo y la ficha de cada historia están en el [documento de historias](https://github.com/riverri/IS1/blob/main/docs/requisitos/historias-de-usuario.md#estimación).

| | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |
|---|---:|---:|---:|---:|---:|
| Historias hechas (45) | 185 | 15.170 | 1.091 | 178 | 179 $ |
| Historias integradas (56; sin estimar HU-102 a HU-107 y HU-111 a HU-113) | 170 | 13.940 | 1.005 | 164 | 164 $ |
| Historias por hacer (1: HU-93, sin estimar) | — | — | — | — | — |
| Historias descartadas (8) | 29 | 2.378 | 171 | 28 | 28 $ |

- **Tamaño:** las líneas de la tabla son puntos × 82 (185 × 82 = 15.170). Medidas en el repositorio son 15.103 líneas de código real para 185 puntos, unas 82 líneas por punto.
- **Sin IA:** unas 14 líneas terminadas por hora. Sale de valores de referencia en tres puntos (25, 15 y 8 líneas/h).
- **Con IA:** se suman cuatro partes.
  - **Dirigir a la IA:** medido.
  - **Revisar el código:** unas 138 líneas/h.
  - **Comprobar cada criterio:** 10 minutos por criterio.
  - **Corregir fallos:** 2,5 fallos por cada mil líneas, medido en la revisión de los 34 fallos.
  
  Revisar es lo que más tiempo lleva.
- **Coste de la IA:** medido con los tokens de la sesión de trabajo y las tarifas de la API. Son unos 11,8 $ por cada mil líneas, casi todo por volver a leer el contexto.

## Por tema

| Tema | Historias |
|---|---|
| Creador de apuestas | HU-01 a HU-07, HU-46, HU-53 |
| General | HU-08 a HU-10 |
| Gestión de usuarios | HU-11 a HU-18, HU-47 |
| Catálogo de eventos | HU-19 a HU-22 |
| Apuesta simple | HU-23 a HU-27, HU-52 |
| Apuestas combinadas | HU-28 a HU-30 |
| Banco de estadísticas | HU-31 a HU-33, HU-49, HU-50 |
| Historial y estadísticas | HU-34, HU-35 |
| Social y notificaciones | HU-36 a HU-43, HU-48, HU-51 |
| Apuestas a largo plazo | HU-44, HU-45 |
