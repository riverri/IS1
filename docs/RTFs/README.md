# Revisiones técnicas formales (RTF)

En esta carpeta van las actas de las **revisiones técnicas formales** del proyecto: reuniones en las que el equipo revisa un producto de trabajo concreto (historias de usuario, código, pruebas, diseño o documentación) para encontrar errores antes de darlo por bueno.

Cada revisión va en su propio archivo, con la fecha y lo que se revisa en el nombre. Por ejemplo, `2026-11-27-historias-de-usuario.md`. Se puede copiar la plantilla de abajo.

## Actas

| Fecha | Qué se revisó | Quién revisó | Dictamen | Documentos |
|---|---|---|---|---|
| 27/11/2026 | Documentación de requisitos, gestión de riesgos y proceso de trabajo (`main`, commit `c21b72d`) | Otro grupo de la asignatura | Aceptado con cambios (21/27) | [Acta](2026-11-27-acta-rtf.pdf) · [Seguimiento de los compromisos](2026-11-27-seguimiento.md) |
| 28/11/2026 | Revisión técnica interna: requisitos, proceso, historial de Git, configuración y código (`main`, commit `7ef7f34`) | Jing Li (del equipo) | Aceptado con modificaciones (15 defectos: 3 altos) | [Seguimiento de los defectos](2026-11-28-seguimiento-revision-interna.md) |

## Plantilla del acta

```markdown
# RTF · <producto revisado>

| | |
|---|---|
| **Fecha y duración** | dd/mm/aaaa · hh:mm a hh:mm (como mucho, 2 horas) |
| **Producto revisado** | Archivo, carpeta o pull request, con su versión (commit) |
| **Autor del producto** | |
| **Jefe de la revisión** | |
| **Revisores** | |
| **Secretario** | Quien escribe este acta |

## Errores encontrados

| # | Dónde | Descripción | Gravedad (grave o menor) | Responsable | Corregido en |
|---|---|---|---|---|---|
| 1 | | | | | |

## Decisión

- [ ] Se acepta sin cambios
- [ ] Se acepta cuando se corrijan los errores menores (no hace falta otra revisión)
- [ ] Se rechaza: hay errores graves y hay que volver a revisarlo

## Seguimiento

Quién comprueba las correcciones y cuándo.
```
