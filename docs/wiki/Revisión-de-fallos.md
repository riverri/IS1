# Revisión de fallos

Se partió de un informe del proyecto con 14 fallos (**F-01 a F-14**) y una revisión completa del código encontró 20 más (**N-01 a N-20**). Los 34 están corregidos en `main`, con 34 pruebas nuevas, y sus issues (#34–#67) están cerrados.

Gravedad: **alta** = puede tumbar la aplicación, perder dinero o dejar entrar a quien no debe · **media** = comportamiento incorrecto · **baja** = molestia o caso raro.

## Del informe

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Issue |
|---|---|---|---|---|
| F-01 · Zona horaria | Alta | El servidor en UTC dejaba apostar hasta 2 horas después de empezar un partido | Reloj en Europe/Madrid y `TZ` en el Dockerfile | [#34](https://github.com/riverri/IS1/issues/34) |
| F-02 · Concurrencia del saldo | Alta | Dos apuestas a la vez podían pisarse | Bloqueo optimista (`@Version`) | [#35](https://github.com/riverri/IS1/issues/35) |
| F-03 · Manipular cuotas | Media | Apostar mucho y cancelar movía las cuotas gratis | Máximo 100 por jugador en el volumen | [#36](https://github.com/riverri/IS1/issues/36) |
| F-04 · Contraseña del creador | Alta | Con el túnel seguía valiendo `creador123` | Contraseña obligatoria en los perfiles públicos | [#37](https://github.com/riverri/IS1/issues/37) |
| F-05 · Sincronización | Alta | Un partido con error deshacía todos | Una transacción por partido | [#38](https://github.com/riverri/IS1/issues/38) |
| F-06 · Cuota de combinadas | Media | Sin tope: error 500 con cuotas enormes | Máximo 1.000 | [#39](https://github.com/riverri/IS1/issues/39) |
| F-07 · Prórroga y penaltis | Media | Se usaba el ganador final | Marcador de los 90 minutos | [#40](https://github.com/riverri/IS1/issues/40) |
| F-08 · Intentos de login | Media | Sin límite | 5 fallos → 15 minutos de bloqueo | [#41](https://github.com/riverri/IS1/issues/41) |
| F-09 · Ranking en memoria | Media | Cargaba todas las apuestas | Consulta agregada | [#42](https://github.com/riverri/IS1/issues/42) |
| F-10 · % de aciertos | Baja | 1 de 1 quedaba primero | Mínimo de 5 apuestas resueltas | [#43](https://github.com/riverri/IS1/issues/43) |
| F-11 · Boleto con GET | Baja | Un enlace GET cambiaba datos | Formulario POST con CSRF | [#44](https://github.com/riverri/IS1/issues/44) |
| F-12 · Saldo negativo | Baja | Sin documentar | Decisión 0002: se mantiene y no se puede apostar | [#45](https://github.com/riverri/IS1/issues/45) |
| F-13 · Volumen y cuentas eliminadas | Media | Combinadas contadas de más; ajustes a cuentas borradas | Importe repartido; cuentas eliminadas intactas | [#46](https://github.com/riverri/IS1/issues/46) |
| F-14 · Consola de H2 | Alta | Abierta a cualquiera | Solo el creador | [#47](https://github.com/riverri/IS1/issues/47) |

## De la revisión del código

| Fallo | Gravedad | Qué pasaba | Cómo se arregló | Issue |
|---|---|---|---|---|
| N-01 · Nombres repetidos | Alta | Impedían arrancar la aplicación | Se rechazan; búsqueda tolerante | [#48](https://github.com/riverri/IS1/issues/48) |
| N-02 · Evento anulado | Alta | Bloqueaba la sincronización | Se salta | [#49](https://github.com/riverri/IS1/issues/49) |
| N-03 · El creador apostaba | Alta | Podía apostar y poner el resultado | Prohibido | [#50](https://github.com/riverri/IS1/issues/50) |
| N-04 · Partidos duplicados | Media | Reaparecían al reiniciar | Datos de ejemplo solo en base vacía | [#51](https://github.com/riverri/IS1/issues/51) |
| N-05 · Cuota cambiada | Media | La simple no avisaba | Aviso con la cuota nueva | [#52](https://github.com/riverri/IS1/issues/52) |
| N-06 · Suspensiones | Media | La API las deshacía | Solo reabre lo que suspendió ella | [#53](https://github.com/riverri/IS1/issues/53) |
| N-07 · XSS en jugadores | Media | Un apóstrofo rompía la confirmación | Nombre en atributo `data-` | [#54](https://github.com/riverri/IS1/issues/54) |
| N-08 · Bloquear bajas | Media | Registrándose con el email anónimo | Dominio reservado | [#55](https://github.com/riverri/IS1/issues/55) |
| N-09 · Contraseñas con tildes | Media | Error 500 por los 72 bytes | Se avisa | [#56](https://github.com/riverri/IS1/issues/56) |
| N-10 · Otras sesiones | Media | Seguían abiertas | Se cierran | [#57](https://github.com/riverri/IS1/issues/57) |
| N-11 · Corregir resultado | Media | Mandaba el marcador viejo | Marcador y resultado deben coincidir | [#58](https://github.com/riverri/IS1/issues/58) |
| N-12 · Aceptar cuotas | Baja | Había que pulsarlo dos veces | Una vez | [#59](https://github.com/riverri/IS1/issues/59) |
| N-13 · 404 en el boleto | Baja | Si un partido empezaba | Aviso | [#60](https://github.com/riverri/IS1/issues/60) |
| N-14 · Ligas huérfanas | Baja | No se podían borrar | Se borran con la baja | [#61](https://github.com/riverri/IS1/issues/61) |
| N-15 · Textos largos | Baja | Error 500 | Validación | [#62](https://github.com/riverri/IS1/issues/62) |
| N-16 · Nota NaN | Baja | Se guardaba 0 | Se rechaza | [#63](https://github.com/riverri/IS1/issues/63) |
| N-17 · Equipo inexistente | Baja | Error 500 | Página 404 | [#64](https://github.com/riverri/IS1/issues/64) |
| N-18 · Plantillas | Baja | Iban al equipo equivocado | Solo equipos sin enlazar | [#65](https://github.com/riverri/IS1/issues/65) |
| N-19 · API colgada | Baja | Sin tiempo máximo | 5 s / 20 s | [#66](https://github.com/riverri/IS1/issues/66) |
| N-20 · Avisos leídos | Baja | Se marcaban los no vistos | Solo los mostrados | [#67](https://github.com/riverri/IS1/issues/67) |

Se dejaron como están a propósito: avisar en el registro de que un email ya existe (lo pide HU-11) y recuperar la contraseña, que es una funcionalidad pendiente (HU-17, [#27](https://github.com/riverri/IS1/issues/27)).
