# 0004 · Umbrales de calidad en la CI

**Estado:** PROPUESTA (21/11/2026). Entró con [riverri/IS1#142](https://github.com/riverri/IS1/pull/142); el equipo la confirma o cambia los números en la próxima reunión.

## Contexto
- La CI solo comprobaba que las pruebas pasaran. La cobertura (JaCoCo) se veía en el resumen, pero podía bajar sin que nada fallara.
- Que una línea esté cubierta no quiere decir que su resultado se compruebe. Las pruebas de mutación (PIT) lo miden: cambian el código a propósito y ven si alguna prueba falla. Al empezar, solo detectaban el 41 % de los cambios en la lógica con pruebas unitarias.

## Decisión
| Comprobación | Umbral | Dónde |
|---|---|---|
| Cobertura de líneas (JaCoCo) | 90 % | `./mvnw verify` |
| Cobertura de ramas (JaCoCo) | 75 % | `./mvnw verify` |
| Análisis estático (SpotBugs) | ningún error de prioridad alta | `./mvnw verify` |
| Mutación (PIT) en cuotas, eventos, equipos, usuarios, estadísticas y formato | 80 % detectadas | trabajo `mutacion` de la CI |
| Recorrido de un jugador en un navegador (`a11y/e2e.js`) | todo bien | trabajo `accesibilidad` de la CI |

Los umbrales están un poco por debajo de lo que había al adoptarlos (93 % de líneas, 80 % de ramas y 86 % de mutación), para que un PR normal no falle por una décima.

## Alternativas descartadas
- **Sin umbrales, solo el informe:** es lo que había y nadie lo miraba.
- **PIT sobre todo el código:** tarda mucho, porque la mayoría de las pruebas arrancan Spring, y daría un número bajo por los controladores, que ya se prueban con MockMvc.
- **SpotBugs con prioridad media:** salen avisos de estilo (campos expuestos en entidades JPA) que no son fallos y obligarían a llenar el filtro de excepciones.

## Consecuencias
- Un PR que baje la cobertura de esos mínimos no se puede fusionar hasta añadir pruebas.
- Si una clase nueva de lógica tiene pruebas unitarias, conviene añadirla a `targetClasses` del perfil `mutacion` en `pom.xml`.
