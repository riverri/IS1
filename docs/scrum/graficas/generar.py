"""Genera las gráficas de velocidad y progreso de las iteraciones del Sprint 1 y el burndown del Sprint 5.

Uso: python3 docs/scrum/graficas/generar.py  (sin dependencias externas).
Los datos salen de las actas de docs/scrum/sprints/iteraciones/ y, los del Sprint 5, de la fecha en que se fusionó
el pull request de cada historia (sus puntos están en docs/requisitos/historias-de-usuario.md).
"""
from pathlib import Path

# (iteración, puntos comprometidos en la planificación, puntos completados, incluido lo añadido durante la iteración)
ITERACIONES = [
    (1, 23, 23),
    (2, 21, 33),
    (3, 24, 24),
    (4, 18, 18),
    (5, 13, 16),
    (6, 18, 18),
    (7, 16, 16),
    (8, 12, 12),
    (9, 12, 16),
    (10, 11, 11),
    (11, 17, 17),
    (12, 10, 10),
]

# Sprint 5 (18/11 – 01/12): (día, puntos de las historias del sprint, puntos ya integrados en main al acabar el día).
# El 17/11, antes de empezar, ya se habían integrado 34 puntos (#113). El 22/11 entraron HU-115 y HU-116 (6 puntos),
# que no estaban planificadas: el alcance sube de 105 a 111. HU-102 a HU-107 y HU-111 a HU-113 no cuentan: sin estimar.
SPRINT_5 = [
    ("18/11", 105, 34), ("19/11", 105, 49), ("20/11", 105, 49), ("21/11", 105, 105), ("22/11", 111, 111),
    ("23/11", 111, 111), ("24/11", 111, 111), ("25/11", 111, 111), ("26/11", 111, 111), ("27/11", 111, 111),
]
DIAS_SPRINT_5 = ["18/11", "19/11", "20/11", "21/11", "22/11", "23/11", "24/11", "25/11", "26/11", "27/11",
                 "28/11", "29/11", "30/11", "01/12"]

SUPERFICIE = "#fcfcfb"
TEXTO = "#0b0b0b"
TEXTO_SUAVE = "#52514e"
REJILLA = "#e4e3df"
COMPROMETIDO = "#eb6834"  # serie 2 de la paleta de referencia
COMPLETADO = "#2a78d6"    # serie 1
ANCHO, ALTO = 760, 380
IZQ, DER, ARRIBA, ABAJO = 56, 24, 64, 48
CARPETA = Path(__file__).parent


def lienzo(titulo, subtitulo):
    return [
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {ANCHO} {ALTO}" width="{ANCHO}" height="{ALTO}" '
        f'font-family="Inter, Segoe UI, Arial, sans-serif" role="img" aria-label="{titulo}">',
        f'<title>{titulo}</title>',
        f'<rect width="{ANCHO}" height="{ALTO}" rx="12" fill="{SUPERFICIE}"/>',
        f'<text x="{IZQ}" y="28" font-size="17" font-weight="700" fill="{TEXTO}">{titulo}</text>',
        f'<text x="{IZQ}" y="48" font-size="12" fill="{TEXTO_SUAVE}">{subtitulo}</text>',
    ]


def eje_y(svg, maximo, paso):
    alto_util = ALTO - ARRIBA - ABAJO
    for valor in range(0, maximo + 1, paso):
        y = ALTO - ABAJO - valor / maximo * alto_util
        svg.append(f'<line x1="{IZQ}" x2="{ANCHO - DER}" y1="{y:.1f}" y2="{y:.1f}" stroke="{REJILLA}" stroke-width="1"/>')
        svg.append(f'<text x="{IZQ - 8}" y="{y + 4:.1f}" font-size="11" text-anchor="end" fill="{TEXTO_SUAVE}">{valor}</text>')
    return alto_util


def leyenda(svg, elementos):
    x = ANCHO - DER
    for color, texto in reversed(elementos):
        ancho_texto = 7 * len(texto)
        x -= ancho_texto
        svg.append(f'<text x="{x}" y="32" font-size="12" fill="{TEXTO_SUAVE}">{texto}</text>')
        x -= 18
        svg.append(f'<rect x="{x}" y="22" width="12" height="12" rx="3" fill="{color}"/>')
        x -= 16


def barra(x, y, ancho, alto, color):
    """Barra con las esquinas de arriba redondeadas (4px) y apoyada en la base."""
    r = min(4, ancho / 2, alto)
    return (f'<path d="M{x:.1f},{y + alto:.1f} V{y + r:.1f} Q{x:.1f},{y:.1f} {x + r:.1f},{y:.1f} '
            f'H{x + ancho - r:.1f} Q{x + ancho:.1f},{y:.1f} {x + ancho:.1f},{y + r:.1f} V{y + alto:.1f} Z" fill="{color}"/>')


def velocidad():
    svg = lienzo("Velocidad por iteración del Sprint 1", "Puntos comprometidos en la planificación de cada iteración y completados al cerrarla")
    leyenda(svg, [(COMPROMETIDO, "Comprometidos"), (COMPLETADO, "Completados")])
    maximo = 40
    alto_util = eje_y(svg, maximo, 10)
    hueco = (ANCHO - IZQ - DER) / len(ITERACIONES)
    ancho_barra = min(26, hueco / 3)
    for i, (iteracion, comprometidos, completados) in enumerate(ITERACIONES):
        centro = IZQ + hueco * (i + 0.5)
        for desplazamiento, valor, color, nombre in ((-ancho_barra - 1, comprometidos, COMPROMETIDO, "comprometidos"),
                                                    (1, completados, COMPLETADO, "completados")):
            alto = valor / maximo * alto_util
            x = centro + desplazamiento
            y = ALTO - ABAJO - alto
            svg.append(f'<g><title>Iteración {iteracion}: {valor} puntos {nombre}</title>{barra(x, y, ancho_barra, alto, color)}</g>')
        svg.append(f'<text x="{centro:.1f}" y="{ALTO - ABAJO - completados / maximo * alto_util - 6:.1f}" font-size="11" '
                   f'text-anchor="start" fill="{TEXTO}">{completados}</text>')
        svg.append(f'<text x="{centro:.1f}" y="{ALTO - ABAJO + 18}" font-size="12" text-anchor="middle" fill="{TEXTO_SUAVE}">It. {iteracion}</text>')
    media = sum(c for _, _, c in ITERACIONES) / len(ITERACIONES)
    y = ALTO - ABAJO - media / maximo * alto_util
    svg.append(f'<line x1="{IZQ}" x2="{ANCHO - DER}" y1="{y:.1f}" y2="{y:.1f}" stroke="{TEXTO_SUAVE}" stroke-width="1" stroke-dasharray="4 4"/>')
    media_texto = f"{media:.1f}".replace(".", ",")
    svg.append(f'<text x="{ANCHO - DER}" y="{y + 14:.1f}" font-size="11" text-anchor="end" fill="{TEXTO_SUAVE}">media {media_texto}</text>')
    svg.append('</svg>')
    (CARPETA / "velocidad.svg").write_text("\n".join(svg), encoding="utf-8")


def progreso():
    total = sum(c for _, _, c in ITERACIONES)
    svg = lienzo("Puntos completados acumulados", f"Total entregado tras cada iteración del Sprint 1 ({total} puntos en {len(ITERACIONES)} iteraciones)")
    maximo = ((total + 49) // 50) * 50
    alto_util = eje_y(svg, maximo, 50)
    hueco = (ANCHO - IZQ - DER) / len(ITERACIONES)
    puntos, acumulado = [], 0
    for i, (iteracion, _, completados) in enumerate(ITERACIONES):
        acumulado += completados
        x = IZQ + hueco * (i + 0.5)
        y = ALTO - ABAJO - acumulado / maximo * alto_util
        puntos.append((iteracion, x, y, acumulado))
        svg.append(f'<text x="{x:.1f}" y="{ALTO - ABAJO + 18}" font-size="12" text-anchor="middle" fill="{TEXTO_SUAVE}">It. {iteracion}</text>')
    camino = " ".join(f"{'M' if i == 0 else 'L'}{x:.1f},{y:.1f}" for i, (_, x, y, _) in enumerate(puntos))
    svg.append(f'<path d="{camino}" fill="none" stroke="{COMPLETADO}" stroke-width="2" stroke-linejoin="round"/>')
    for i, (iteracion, x, y, valor) in enumerate(puntos):
        svg.append(f'<g><title>Iteración {iteracion}: {valor} puntos acumulados</title>'
                   f'<circle cx="{x:.1f}" cy="{y:.1f}" r="4.5" fill="{COMPLETADO}" stroke="{SUPERFICIE}" stroke-width="2"/></g>')
        if i in (0, len(puntos) - 1) or i % 2 == 1:
            svg.append(f'<text x="{x:.1f}" y="{y - 10:.1f}" font-size="11" text-anchor="middle" fill="{TEXTO}">{valor}</text>')
    svg.append('</svg>')
    (CARPETA / "progreso.svg").write_text("\n".join(svg), encoding="utf-8")


def burndown():
    svg = lienzo("Burndown del Sprint 5", "Puntos de historia que faltan por integrar en main al acabar cada día, frente al ritmo ideal")
    leyenda(svg, [(COMPROMETIDO, "Ideal"), (COMPLETADO, "Pendiente")])
    inicial = SPRINT_5[0][1] - SPRINT_5[0][2]
    maximo = ((inicial + 19) // 20) * 20
    alto_util = eje_y(svg, maximo, 20)
    hueco = (ANCHO - IZQ - DER) / len(DIAS_SPRINT_5)

    def x(i):
        return IZQ + hueco * (i + 0.5)

    def y(valor):
        return ALTO - ABAJO - valor / maximo * alto_util

    ultimo = len(DIAS_SPRINT_5) - 1
    svg.append(f'<path d="M{x(0):.1f},{y(inicial):.1f} L{x(ultimo):.1f},{y(0):.1f}" fill="none" stroke="{COMPROMETIDO}" '
               f'stroke-width="2" stroke-dasharray="6 4"/>')
    puntos = [(i, dia, alcance - hecho) for i, (dia, alcance, hecho) in enumerate(SPRINT_5)]
    camino = " ".join(f"{'M' if k == 0 else 'L'}{x(i):.1f},{y(v):.1f}" for k, (i, _, v) in enumerate(puntos))
    svg.append(f'<path d="{camino}" fill="none" stroke="{COMPLETADO}" stroke-width="2" stroke-linejoin="round"/>')
    for i, dia, valor in puntos:
        svg.append(f'<g><title>{dia}: faltan {valor} puntos</title>'
                   f'<circle cx="{x(i):.1f}" cy="{y(valor):.1f}" r="4.5" fill="{COMPLETADO}" stroke="{SUPERFICIE}" stroke-width="2"/></g>')
        if i < 4:
            svg.append(f'<text x="{x(i):.1f}" y="{y(valor) - 10:.1f}" font-size="11" text-anchor="middle" fill="{TEXTO}">{valor}</text>')
    for i, dia in enumerate(DIAS_SPRINT_5):
        if i % 2 == 0 or i == ultimo:
            svg.append(f'<text x="{x(i):.1f}" y="{ALTO - ABAJO + 18}" font-size="11" text-anchor="middle" fill="{TEXTO_SUAVE}">{dia}</text>')
    svg.append('</svg>')
    (CARPETA / "burndown-sprint-5.svg").write_text("\n".join(svg), encoding="utf-8")


if __name__ == "__main__":
    velocidad()
    progreso()
    burndown()
    print("Gráficas generadas en", CARPETA)
