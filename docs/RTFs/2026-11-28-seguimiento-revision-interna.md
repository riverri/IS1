# Seguimiento de la revisión técnica interna del 28/11/2026

Revisión hecha por Jing Li sobre el producto propio (`main`, commit `7ef7f34`). **Veredicto: aceptado con modificaciones.** Encontró 15 defectos: 3 altos, 6 medios y 6 bajos. A diferencia de la [RTF del 27/11](2026-11-27-seguimiento.md), esta también revisa el código y el historial de Git.

Esta tabla recoge qué se ha hecho con cada defecto y qué queda pendiente.

| Defecto | Severidad | Qué se ha hecho (28/11) | Qué falta |
|---|---|---|---|
| B-01 · Registro auxiliar sin verificar el correo, activo en producción | Alta | — | **Decisión del equipo:** desactivarlo en producción, o dejarlo como plan B si el correo de Render falla, con límite de envíos, una decisión documentada y su historia |
| B-02 · Commits directos a `main` | Alta | — | **Decisión del equipo:** activar la protección de `main` en GitHub (hace falta permiso de administrador) |
| B-03 · PR fusionados por su único autor, algunos muy grandes | Alta | — | **Decisión del equipo:** exigir la aprobación de otro miembro en la protección de la rama y limitar cada PR a una historia |
| B-04 · Trabajo concentrado en una cuenta | Media | — | Repartir las historias que queden y apuntar quién hace cada una |
| B-05 · Actas de los Sprints 3 a 5 incompletas | Media | Los huecos que se podían rellenar desde el repositorio ya se rellenaron con la RTF | Lo que sale de las reuniones del Sprint 5 (capacidad, horas reales, review y retrospectiva) |
| B-06 · Métricas paradas en el Sprint 2, sin burndown | Media | [metricas.md](../scrum/metricas.md): puntos de los Sprints 3 (2), 4 (65) y 5 (111, en curso) y burndown del Sprint 5 con su gráfica, calculado con las fechas de integración de cada historia | Llevar el burndown en las dailies del Sprint 6 |
| B-07 · Totales que no cuadran entre documentos | Media | Script `scripts/totales.py`, que calcula los totales y los escribe en [totales.md](../requisitos/totales.md). La CI comprueba en cada PR que están al día y que cada prueba citada en la trazabilidad existe. Ahora son los mismos en historias, backlog, INVEST y wiki: 114 historias, 289 criterios y 780 pruebas en 125 clases | — |
| B-08 · Criterios sin prueba e historias sin estimar | Media | Pruebas de los seis criterios: `BloqueoLigaRechazadoTest` (CA-105.2, CA-105.5, CA-106.3) y `ClasificacionLigasWebTest` (CA-111.4, CA-112.2, CA-113.5), 15 pruebas en total. Ya no queda ningún criterio de historias hechas o integradas sin prueba; solo dos sin hacer (HU-107) | **Estimar HU-102 a HU-107 y HU-111 a HU-113:** sus autores |
| B-09 · Funcionalidades sin historia | Media | HU-11 (Registro) se actualiza al registro con confirmación por correo: CA-11.1 y CA-11.2 reescritos, CA-11.4 nuevo, y fuera de la trazabilidad las referencias a pruebas borradas. Historias nuevas, con fila 60 del backlog, trazabilidad y una prueba más: HU-115 (Recordar este navegador) y HU-116 (Cambiar el correo de mi cuenta) | — |
| B-10 · Sin etiquetas de Git por sprint | Baja | — | Etiquetar el cierre del Sprint 5 (01/12) |
| B-11 · Decisión 0006 sin actualizar | Baja | — | Poner su estado según lo que haya pasado con el correo en Render |
| B-12 · Documentos sin mantener | Baja | `RevisionCódigo1.md`: los cuatro paquetes sin revisar se retiran como pendientes, explicando por qué y dónde está su documentación. En la estimación se aclara que las 15.103 líneas son las de la calibración inicial; hoy el Java de la aplicación tiene 26.938 | — |
| B-13 · Un GET apunta clics de anuncios | Baja | Las precargas del navegador (`Sec-Purpose`, `Purpose`, `X-Moz`) y los rastreadores siguen el enlace, pero ya no cuentan como clic. Tiene su prueba | — |
| B-14 · La imagen Docker se ejecuta como root | Baja | El `Dockerfile` crea el usuario `apuestas` y ejecuta la aplicación con él. Además, se ajusta la memoria de Java al plan gratuito de Render (el 25/11 se reinició por pasar de 512 MB): medido en local, de unos 640 MB a unos 430 MB | Comprobar el primer despliegue en Render (en el entorno de trabajo no hay Docker) |
| B-15 · Ramas sin borrar | Baja | — | **Decisión del equipo:** qué hacer con `codex/optimizacion-fluidez` (25 commits que no están en `main`) y borrar las ramas ya fusionadas |

**Además (28/11):** la pestaña o el enlace del menú activos se marcaban solo con el color. Ahora llevan `aria-current="page"` para que un lector de pantalla diga cuál es (menú, deportes, ranking, equipos, ligas y fantasy). Lo vio quien escribió las pruebas de CA-111.4.
