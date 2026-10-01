# Visión del producto

## Problema / oportunidad
A los aficionados al deporte les gusta pronosticar resultados, pero las casas de apuestas implican dinero real y riesgos legales y de adicción.
Queremos una web que ofrezca la experiencia de una casa de apuestas digital **con moneda virtual ("moneditas"), sin dinero real**.

## Actores
- **Visitante**: consulta el catálogo de eventos y las cuotas sin registrarse.
- **Usuario / apostador**: se registra, recibe moneditas, hace apuestas simples y combinadas y cobra automáticamente.
- **Creador de apuestas** (administrador): da de alta competiciones, equipos y eventos, ajusta la forma de los equipos, introduce resultados, suspende o anula eventos y fija límites.

## Funcionalidades clave
1. **Cuotas calculadas por nuestro propio algoritmo** (G/E/P), a partir de:
   - la **calificación de calidad** (0–10) de cada equipo o deportista,
   - un **factor de forma** reciente ajustable a mano (rachas, moral, lesiones),
   - el **volumen apostado** en cada resultado: cuanto más se apuesta a una opción, más baja su cuota (con un mínimo, p. ej. 1,01),
   - un **margen de la casa**: Σ(1/cuota) > 1.
2. **Apuestas combinadas**: multiplicador total = producto de las cuotas; ganancia potencial = importe × multiplicador. Solo se cobra si se aciertan todas las selecciones; un evento anulado cuenta con cuota 1,00.
3. **Resolución automática** de las apuestas cuando el creador introduce el resultado.
4. **Banco de estadísticas** de equipos: ficha, últimos resultados, evolución y comparativa cara a cara (después del MVP).

## MVP
Un usuario puede registrarse, recibir sus moneditas, ver el catálogo con las cuotas calculadas por nuestro algoritmo, hacer apuestas simples y combinadas, y cobrar automáticamente cuando el creador de apuestas introduce el resultado.
Los equipos y resultados se cargan **a mano**, sin API. Ver [Product Backlog](../scrum/product-backlog.md).

## Fuera de alcance (por ahora)
- Dinero real o pagos.
- API externa de datos, estadísticas de equipos y parte social: quedan para después del MVP.

## Documentación relacionada
- [Historias de usuario](historias-de-usuario.md)
- [Product Backlog](../scrum/product-backlog.md)
