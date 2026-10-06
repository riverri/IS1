# 0002 · Saldo negativo al corregir un resultado

**Estado:** ADOPTADA.

## Contexto
- Cuando el creador de apuestas corrige un resultado (o un marcador, o el ganador de un mercado), las apuestas se vuelven a resolver y los saldos se ajustan: a quien cobró sin deber se le quita lo cobrado.
- Si ese jugador ya se había gastado las ganancias en otras apuestas, su saldo puede quedar por debajo de 0.

## Decisión
- Se **permite el saldo negativo** como resultado de una corrección. Es una deuda con la casa, no un error.
- Mientras el saldo sea negativo (o no llegue al importe), el jugador **no puede apostar**: `Usuario.cargar` lo rechaza igual que con cualquier saldo insuficiente.
- La recarga gratuita se suma al saldo que haya, así que primero cubre la deuda.
- Las cuentas eliminadas no se ajustan: su saldo ya no cuenta para nada.

## Alternativas descartadas
- **Dejar el saldo en 0:** la casa perdería el dinero pagado por error y el ranking por saldo premiaría haber cobrado una apuesta mal resuelta.
- **Anular las apuestas hechas con ese dinero:** obligaría a deshacer apuestas de otros partidos que no tienen nada que ver con la corrección, y algunas ya estarían resueltas.
- **No dejar corregir resultados ya pagados:** un resultado mal introducido (HU-04) se quedaría así para siempre.

## Consecuencias
- En el ranking por saldo un jugador puede aparecer con saldo negativo.
- Corregir dos veces y volver al resultado original deja el saldo como estaba: los ajustes son siempre por la diferencia.
