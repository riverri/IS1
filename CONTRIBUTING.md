# Cómo contribuir

## Ramas
- `main`: siempre funcional. **Nadie hace push directo**: solo se entra por Pull Request.
- Una rama por issue: `tipo/NUM-descripcion-corta`, p. ej. `feature/6-apuesta-combinada`, `fix/21-saldo-negativo`, `docs/3-backlog`.

## Flujo de trabajo
1. Coge un issue del Sprint Backlog, asígnatelo y muévelo a **En curso**.
2. `git checkout main && git pull`
3. `git checkout -b feature/6-apuesta-combinada`
4. Trabaja con commits pequeños (ver formato abajo).
5. `git push -u origin feature/6-apuesta-combinada` y abre un Pull Request hacia `main` con `Closes #6` en la descripción.
6. Al menos **1 compañero** revisa y aprueba el PR. Después se hace merge (squash) y se borra la rama.

## Mensajes de commit
Formato: `tipo: descripción en presente`
- `feat:` nueva funcionalidad · `fix:` corrección · `docs:` documentación · `test:` pruebas · `refactor:` reestructuración sin cambiar el comportamiento · `chore:` configuración y mantenimiento

Ejemplo: `feat: calcula el multiplicador de apuestas combinadas (#6)`

## Revisión de código
- ¿Cumple los criterios de aceptación del issue?
- ¿Se entiende el código? ¿Tiene pruebas?
- Comentarios constructivos y concretos.

## Cambios en la base de datos
Todo cambio en las entidades JPA necesita su script de migración en `src/main/resources/db/migration` (`V<n>__descripcion.sql`). Los scripts que ya están en `main` no se modifican nunca. Más detalles en el README.
