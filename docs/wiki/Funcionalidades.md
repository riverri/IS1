# Funcionalidades

Qué ve el usuario, qué reglas se aplican y en qué clases está cada cosa.

## Usuarios, sesión y moneditas

### Registro (HU-11)
1. `/registro` pide email, nombre y contraseña. `RegistroForm` valida formato y longitudes.
2. `UsuarioService.crear` pasa el email a minúsculas y comprueba que no exista.
3. La contraseña se cifra con **BCrypt** (no se puede recuperar la original; máximo 72 bytes).
4. El usuario empieza con **1.000 moneditas** (HU-14).

### Inicio de sesión (HU-12)
Spring Security con el formulario `/login`. Tras 5 contraseñas incorrectas, la cuenta se bloquea 15 minutos. Al entrar, `RecargaAlEntrarHandler` aplica la recarga si toca.

### Saldo y recargas (HU-13, HU-14)
- El saldo se ve siempre en la cabecera.
- Cada **7 días** se suman **200 moneditas** gratis (al iniciar sesión o abrir *Mi cuenta*). No se acumulan.
- El saldo cambia con `cargar` (al apostar; si no hay saldo, `SaldoInsuficienteException`), `abonar` (cancelar, cobrar) y `ajustar` (resolver o corregir).
- Si dos operaciones cambian el mismo saldo a la vez, la segunda se rechaza con un aviso (bloqueo optimista).

### Mi cuenta (HU-47)
Cambiar nombre y contraseña (pidiendo la actual). Al cambiar la contraseña se cierran las sesiones abiertas en otros navegadores.

## Apuesta simple (HU-23)
1. En `apostar.html` el jugador elige el resultado y el importe; JavaScript enseña la ganancia potencial.
2. `ApuestaController` valida el formulario y llama a `ApuestaService.apostar`.
3. El servicio, en una transacción:
   - comprueba los límites de la casa (importe 1–500, número de selecciones);
   - comprueba que no es el creador;
   - comprueba que el evento admite apuestas (programado y sin empezar);
   - calcula la cuota actual y, si es distinta de la que vio el jugador, se lo enseña para que decida;
   - comprueba la cuota total máxima (1.000) y el juego responsable;
   - descuenta el saldo y guarda la apuesta.
4. Redirige a `/apuestas` con "Apuesta realizada" o vuelve al formulario con el error.

**Cancelar y cambiar el importe (HU-26, HU-27):** mientras ninguno de sus partidos haya empezado. Al cancelar se devuelve todo; al cambiar, se cobra o devuelve la diferencia con las cuotas del momento.

## Boleto y combinadas (HU-28, HU-29, HU-30)
- Al pulsar una cuota, la selección va al **boleto** (vive en la sesión).
- No se repite un partido: otro resultado del mismo partido sustituye al anterior.
- Máximo 10 selecciones (límite editable).
- **Cuota total = producto de las cuotas.** Ejemplo: 1,80 × 2,10 × 1,50 = 5,67; con 10 moneditas se ganan 56,70. Máximo 1.000.
- Con una sola selección es una apuesta simple.
- Si una cuota cambia, se enseñan las nuevas y hay que aceptarlas.
- Se gana solo si se aciertan **todas**; un partido anulado cuenta con cuota 1,00.

## Resolver las apuestas (HU-04, HU-05, HU-25)
Al introducir el resultado (el creador o la API), `ResolucionService.introducirResultado`:
1. Marca el evento como `FINALIZADO`.
2. Para cada selección de ese evento llama a `apuesta.resolver(...)`.
3. La selección queda `ACERTADA` o `FALLADA` y la apuesta se reevalúa.
4. Si la apuesta cambia de estado, el jugador recibe un aviso.

**La regla de `Apuesta.reevaluar()`:**
- alguna selección fallada → **PERDIDA** (debe pagarse 0);
- si no, alguna pendiente → **ACTIVA**;
- todas anuladas → **ANULADA** (se devuelve el importe);
- si no → **GANADA** (importe × producto de cuotas).

Después `usuario.ajustar(loQueDebePagar − loYaPagado)`. Así **corregir un resultado** funciona solo: se quita o se paga la diferencia. El saldo puede quedar negativo; es una decisión documentada en [`docs/decisiones/0002`](https://github.com/riverri/IS1/blob/main/docs/decisiones/0002-saldo-negativo-al-corregir.md): mientras sea negativo no se puede apostar.

**Suspender** solo impide apostar. **Anular** pone las selecciones con cuota 1,00 y reevalúa las apuestas.

## Otros mercados de fútbol (HU-52)
Doble oportunidad, más/menos de 2,5 goles y ambos marcan (ver [Algoritmo de cuotas](Algoritmo-de-cuotas)). En fútbol el creador introduce el **marcador** y el resultado sale de ahí. Si solo se marca el ganador, las de goles y ambos marcan se anulan.

## Apuestas a largo plazo (HU-44, HU-45)
Un **mercado** ("¿Quién ganará el Balón de Oro?") tiene varios **candidatos** con cuotas fijadas por el creador. Se apuesta en `/mercados` hasta el cierre. El creador puede cerrarlo, marcar el ganador (se resuelve igual que un partido), corregirlo o anularlo (se devuelve todo).

## Panel del creador
| Función | Detalles |
|---|---|
| Alta de competiciones y equipos (HU-01) | Con deporte y calidad 0–10. Sin nombres repetidos |
| Editar un equipo (HU-02, HU-50) | Calidad, forma, escudo y jugadores con su nota |
| Alta de eventos | Competición, local y visitante distintos, fecha y fase |
| Editar o borrar un evento (HU-46) | Solo si no ha empezado. Con apuestas no se cambian los equipos ni se borra: se anula |
| Detalle de un evento (HU-04, HU-05, HU-06) | Dinero apostado a cada resultado; resultado o marcador (debe coincidir); suspender, reactivar o anular |
| Límites (HU-07) | Importe mínimo y máximo, selecciones por combinada, interruptor del juego responsable |
| Mercados (HU-45) | Crear, candidatos, cuotas, cerrar, ganador, anular |
| Cuentas de la casa (HU-54) | Apostado, pagado, beneficio, en juego; margen real frente al teórico (6,5 %); por tipo y por deporte |
| Sincronizar ahora / Descargar plantillas | Lanza a mano la sincronización con la API |

## Estadísticas de equipos
- **Ficha** (`/equipos/{id}`, HU-31): últimos 10 resultados, racha de 5, balance, clasificación (3 puntos por victoria, 1 por empate) y próximos 5 partidos.
- **Gráfico de evolución** (HU-32): puntos acumulados partido a partido.
- **Plantilla y alineación** (HU-49): jugadores por posición con su nota; alineación probable en 4-3-3.
- **Cara a cara** (HU-33): racha de cada equipo y enfrentamientos anteriores.

## Ranking, perfiles, ligas y avisos
- **Ranking** (HU-36): por saldo, ganancias o % de aciertos (mínimo 5 apuestas resueltas). No aparecen el creador ni las cuentas eliminadas.
- **Perfil público** (HU-48): puesto, saldo, aciertos y balance, sin enseñar las apuestas.
- **Ligas privadas** (HU-51): código de 6 caracteres sin letras confusas (sin 0/O ni 1/I/L); ranking solo entre sus miembros; máximo 10 ligas por jugador; el creador la borra y los demás salen.
- **Avisos** (HU-37): al ganar, perder o anularse una apuesta; la campana muestra los no leídos.

## Juego responsable y baja
- **Límites personales** (HU-10): máximo apostado en 24 horas y en 7 días.
- **Pausa:** 1, 7 o 30 días sin apostar. El creador puede desactivar el juego responsable.
- **Eliminar la cuenta** (HU-18): pide la contraseña. Se cancelan las apuestas cancelables, se borran avisos y ligas propias, se cierran las otras sesiones y la cuenta se **anonimiza** (email `eliminado-<id>@apuestas.invalid`, nombre "Usuario eliminado", saldo 0). La fila se conserva para que las cuentas de la casa cuadren.
