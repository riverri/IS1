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

*(Regla propuesta por la IA; el equipo puede personalizarla.)* **Si hay varios PR listos, se fusionan de uno en uno:** el siguiente, cuando el CI de `main` del anterior esté en verde. Un PR que se hizo encima de otro (apilado) no se fusiona mientras su base no sea `main`. Si el PR trae una migración, su número tiene que ser mayor que la última que hay en `main` en ese momento (ver [I-01](Revisión-de-fallos#al-integrar)).

## Estilo del código
- El repositorio trae un [`.editorconfig`](https://github.com/riverri/IS1/blob/main/.editorconfig) que IntelliJ, VS Code y Eclipse leen solos: UTF-8, saltos de línea de Unix, 4 espacios y sin espacios al final.
- `./mvnw verify` comprueba además unas pocas reglas de Java ([`checkstyle.xml`](https://github.com/riverri/IS1/blob/main/checkstyle.xml)) y falla si no se cumplen:
  - líneas de 140 caracteres como mucho;
  - sin tabuladores;
  - sin imports que no se usan;
  - `if`, `for` y `while` siempre con llaves.

  Suelto: `./mvnw checkstyle:check`.
- En las plantillas no se escribe JavaScript (ni `onclick` ni `<script>` dentro de la página), porque la política de seguridad no lo dejaría ejecutar. Para pedir confirmación se usa `data-confirmar="¿Seguro?"`, y el resto va en un archivo de `static/js`.

## Mensajes de commit
`tipo: descripción en presente`: `feat:` funcionalidad · `fix:` corrección · `docs:` documentación · `test:` pruebas · `refactor:` reestructuración · `chore:` configuración.

## Si el cambio toca una entidad
Añade `V11__descripcion.sql` en `db/migration/h2/` **y** en `db/migration/postgresql/`. Nunca modifiques un script que ya esté en `main` (ver [Base de datos y migraciones](Base-de-datos-y-migraciones)).

## Al terminar una historia
Añade sus criterios de aceptación (CA-xx.y) a la [matriz de trazabilidad](https://github.com/riverri/IS1/blob/main/docs/requisitos/trazabilidad.md) con la prueba de cada uno.
