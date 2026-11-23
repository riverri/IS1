# 0007 · Jornada de la liga fantasy

**Estado:** ACEPTADA (21/11/2026). El equipo la puede cambiar si prefiere otra regla.

## Contexto
- En la liga fantasy (HU-81 a HU-83), cada fichaje suma los partidos de su club que empiezan mientras está en el equipo (CA-82.1).
- Las historias no decían si había cierre de jornada o un límite de cambios. Sin ninguno, se podía vender a un jugador cuyo partido ya se había jugado y fichar a otro que jugaba más tarde. Así un mismo hueco sumaba los puntos de varios partidos de la misma jornada (lo encontró la cuarta depuración).

## Decisión
- La **jornada** es una semana, de lunes a lunes (hora de Madrid). Empieza con su primer partido de fútbol, sin contar los suspendidos ni los anulados: es el **plazo** para hacer cambios.
- **Hasta el plazo**, fichar y vender cuenta enseguida, como antes.
- **Después del plazo**, cuenta desde la jornada siguiente:
  - el que se ficha ocupa su hueco y gasta el presupuesto enseguida, pero suma desde el lunes;
  - el que se vende deja su hueco y devuelve el presupuesto enseguida, pero sigue sumando lo que queda de jornada.
- Es la regla de las ligas fantasy más jugadas (los cambios después del plazo valen para la jornada siguiente). Cada hueco suma como mucho a un jugador en cada jornada.
- La página de la fantasy y el mercado avisan del plazo, o de que la jornada ya ha empezado. Cada jugador que aún no suma lo dice («Suma desde el lunes 24/11»).
- Se añade el criterio CA-82.3.

## Alternativas descartadas
- **Cerrar el mercado durante la jornada:** también es habitual, pero no deja fichar a quien acaba de crear su equipo un sábado. Con la regla elegida puede fichar, y sus jugadores suman desde el lunes.
- **Jornada según la fase de cada partido** («Jornada 8»): los clubes juegan varias competiciones y las fases no coinciden. Una semana fija vale para todas.
- **Límite de cambios por jornada:** no evita sumar dos partidos en el mismo hueco.

## Consecuencias
- No hace falta migración: se sigue guardando desde y hasta cuándo suma cada fichaje (`desde`, `hasta`). Solo cambia el valor que se apunta.
- Un partido del lunes por la noche es de la jornada siguiente a la del fin de semana. Cada semana queda cerrada en sí misma, así que no se suma nada dos veces.
