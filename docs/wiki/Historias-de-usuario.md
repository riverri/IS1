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

La [matriz de trazabilidad](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md) usa los mismos identificadores para enlazar cada criterio con su prueba.

## Estado

| Estado | Historias |
|---|---|
| **Hechas (45)** | HU-01 a HU-14, HU-17 a HU-37, HU-44 a HU-52 y HU-54 |
| **Descartadas (8)** | HU-15 y HU-16 (cuenta bancaria y retiradas: chocan con "sin dinero real") y HU-38 a HU-43 (seguir usuarios, feed, privacidad, copiar apuestas). En su lugar se hicieron el perfil público (HU-48) y las ligas (HU-51) |

En total hay **107 criterios de aceptación**; 94 son de historias hechas.

## Por tema

| Tema | Historias |
|---|---|
| Creador de apuestas | HU-01 a HU-07, HU-46, HU-54 |
| General | HU-08 a HU-10 |
| Gestión de usuarios | HU-11 a HU-18, HU-47 |
| Catálogo de eventos | HU-19 a HU-22 |
| Apuesta simple | HU-23 a HU-27, HU-52 |
| Apuestas combinadas | HU-28 a HU-30 |
| Banco de estadísticas | HU-31 a HU-33, HU-49, HU-50 |
| Historial y estadísticas | HU-34, HU-35 |
| Social y notificaciones | HU-36 a HU-43, HU-48, HU-51 |
| Apuestas a largo plazo | HU-44, HU-45 |
