# Cómo contribuir

El flujo completo está en [`CONTRIBUTING.md`](https://github.com/riverri/IS1/blob/main/CONTRIBUTING.md).

## Ramas
- `main` siempre funciona. **Nadie hace push directo**: solo se entra por pull request.
- Una rama por issue: `tipo/NUM-descripcion-corta`, por ejemplo `feature/80-nueva-funcion` o `fix/81-saldo`.

## Paso a paso
1. En el tablero, asígnate el issue y muévelo a **En curso**.
2. Actualiza `main` y crea la rama:
   ```
   git checkout main
   git pull
   git checkout -b feature/80-nueva-funcion
   ```
3. Haz el cambio y añade sus pruebas.
4. Comprueba que todo pasa: `./mvnw test`.
5. Guarda y sube:
   ```
   git add .
   git commit -m "feat: descripción del cambio (#80)"
   git push -u origin feature/80-nueva-funcion
   ```
6. En GitHub, abre un **pull request** hacia `main` con `Closes #80` en la descripción.
7. Espera al CI en verde y a que un compañero lo revise y apruebe.
8. Fusiona con **Squash and merge** y borra la rama.

## Mensajes de commit
`tipo: descripción en presente`: `feat:` funcionalidad · `fix:` corrección · `docs:` documentación · `test:` pruebas · `refactor:` reestructuración · `chore:` configuración.

## Si el cambio toca una entidad
Añade `V11__descripcion.sql` en `db/migration/h2/` **y** en `db/migration/postgresql/`. Nunca modifiques un script que ya esté en `main` (ver [Base de datos y migraciones](Base-de-datos-y-migraciones)).

## Al terminar una historia
Añade sus criterios de aceptación (CA-xx.y) a la [matriz de trazabilidad](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md) con la prueba de cada uno.
