"""Genera las gráficas de velocidad y progreso a partir de los puntos de cada sprint.

Uso: python3 docs/scrum/graficas/generar.py  (sin dependencias externas).
Al cerrar un sprint, añade su fila a SPRINTS y vuelve a ejecutarlo.
"""
from pathlib import Path

# (sprint, puntos comprometidos en la planificación, puntos completados, incluido lo añadido durante el sprint)
SPRINTS = [
    (1, 23, 23),
    (2, 21, 33),
    (3, 24, 24),
    (4, 18, 18),
    (5, 13, 16),
    (6, 18, 18),
    (7, 16, 16),
    (8, 12, 12),
]

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
    svg = lienzo("Velocidad por sprint", "Puntos de historia comprometidos en la planificación y completados al cerrar")
    leyenda(svg, [(COMPROMETIDO, "Comprometidos"), (COMPLETADO, "Completados")])
    maximo = 40
    alto_util = eje_y(svg, maximo, 10)
    hueco = (ANCHO - IZQ - DER) / len(SPRINTS)
    ancho_barra = min(26, hueco / 3)
    for i, (sprint, comprometidos, completados) in enumerate(SPRINTS):
        centro = IZQ + hueco * (i + 0.5)
        for desplazamiento, valor, color, nombre in ((-ancho_barra - 1, comprometidos, COMPROMETIDO, "comprometidos"),
                                                    (1, completados, COMPLETADO, "completados")):
            alto = valor / maximo * alto_util
            x = centro + desplazamiento
            y = ALTO - ABAJO - alto
            svg.append(f'<g><title>Sprint {sprint}: {valor} puntos {nombre}</title>{barra(x, y, ancho_barra, alto, color)}</g>')
        svg.append(f'<text x="{centro:.1f}" y="{ALTO - ABAJO - completados / maximo * alto_util - 6:.1f}" font-size="11" '
                   f'text-anchor="start" fill="{TEXTO}">{completados}</text>')
        svg.append(f'<text x="{centro:.1f}" y="{ALTO - ABAJO + 18}" font-size="12" text-anchor="middle" fill="{TEXTO_SUAVE}">Sprint {sprint}</text>')
    media = sum(c for _, _, c in SPRINTS) / len(SPRINTS)
    y = ALTO - ABAJO - media / maximo * alto_util
    svg.append(f'<line x1="{IZQ}" x2="{ANCHO - DER}" y1="{y:.1f}" y2="{y:.1f}" stroke="{TEXTO_SUAVE}" stroke-width="1" stroke-dasharray="4 4"/>')
    svg.append(f'<text x="{ANCHO - DER}" y="{y - 6:.1f}" font-size="11" text-anchor="end" fill="{TEXTO_SUAVE}">media {media:.1f}</text>')
    svg.append('</svg>')
    (CARPETA / "velocidad.svg").write_text("\n".join(svg), encoding="utf-8")


def progreso():
    total = sum(c for _, _, c in SPRINTS)
    svg = lienzo("Puntos completados acumulados", f"Total entregado tras cada sprint ({total} puntos en {len(SPRINTS)} sprints)")
    maximo = ((total + 49) // 50) * 50
    alto_util = eje_y(svg, maximo, 50)
    hueco = (ANCHO - IZQ - DER) / len(SPRINTS)
    puntos, acumulado = [], 0
    for i, (sprint, _, completados) in enumerate(SPRINTS):
        acumulado += completados
        x = IZQ + hueco * (i + 0.5)
        y = ALTO - ABAJO - acumulado / maximo * alto_util
        puntos.append((sprint, x, y, acumulado))
        svg.append(f'<text x="{x:.1f}" y="{ALTO - ABAJO + 18}" font-size="12" text-anchor="middle" fill="{TEXTO_SUAVE}">Sprint {sprint}</text>')
    camino = " ".join(f"{'M' if i == 0 else 'L'}{x:.1f},{y:.1f}" for i, (_, x, y, _) in enumerate(puntos))
    svg.append(f'<path d="{camino}" fill="none" stroke="{COMPLETADO}" stroke-width="2" stroke-linejoin="round"/>')
    for i, (sprint, x, y, valor) in enumerate(puntos):
        svg.append(f'<g><title>Sprint {sprint}: {valor} puntos acumulados</title>'
                   f'<circle cx="{x:.1f}" cy="{y:.1f}" r="4.5" fill="{COMPLETADO}" stroke="{SUPERFICIE}" stroke-width="2"/></g>')
        if i in (0, len(puntos) - 1) or i % 2 == 1:
            svg.append(f'<text x="{x:.1f}" y="{y - 10:.1f}" font-size="11" text-anchor="middle" fill="{TEXTO}">{valor}</text>')
    svg.append('</svg>')
    (CARPETA / "progreso.svg").write_text("\n".join(svg), encoding="utf-8")


if __name__ == "__main__":
    velocidad()
    progreso()
    print("Gráficas generadas en", CARPETA)
