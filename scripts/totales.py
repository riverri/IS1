"""Totales del proyecto, calculados siempre de la misma fuente (defecto B-07 de la revisión técnica interna).

Uso:
    python3 scripts/totales.py              # escribe docs/requisitos/totales.md
    python3 scripts/totales.py --comprobar  # falla si totales.md no está al día o algo no cuadra (lo usa la CI)

Fuentes:
- Historias, estados, criterios y estimación: la tabla «Resumen» y las fichas de docs/requisitos/historias-de-usuario.md.
- Pruebas: los métodos con @Test, @ParameterizedTest, @RepeatedTest o @TestFactory de src/test/java.
- Trazabilidad: docs/requisitos/trazabilidad.md (cada prueba que cita tiene que existir).

Los demás documentos citan estas cifras o enlazan a totales.md; si cambian las historias o las pruebas, se vuelve a
ejecutar este script y se copian las cifras nuevas.
"""
import re
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
HISTORIAS = RAIZ / "docs/requisitos/historias-de-usuario.md"
TRAZABILIDAD = RAIZ / "docs/requisitos/trazabilidad.md"
PRUEBAS = RAIZ / "src/test/java"
SALIDA = RAIZ / "docs/requisitos/totales.md"

GRUPOS = ["Hechas", "Integradas", "Propuestas", "Descartadas"]


def numero(texto):
    """'1.091' → 1091, '2,9 $' → 2.9, '—' → 0."""
    texto = texto.replace("$", "").replace(".", "").replace(",", ".").strip()
    try:
        return float(texto)
    except ValueError:
        return 0.0


def grupo(estado):
    if estado.startswith("Hecha"):
        return "Hechas"
    if estado.startswith("Integrada"):
        return "Integradas"
    if estado.startswith("Propuesta"):
        return "Propuestas"
    if estado.startswith("Descartada"):
        return "Descartadas"
    return "Por hacer"


def leer_historias():
    texto = HISTORIAS.read_text(encoding="utf-8")
    resumen = {}
    for linea in texto.split("\n"):
        m = re.match(r"^\| HU-(\d+) \|", linea)
        if m:
            c = [x.strip() for x in linea.split("|")[1:-1]]
            resumen[int(m.group(1))] = {
                "titulo": c[1], "estado": c[2], "criterios": int(c[3]), "puntos": c[5],
                "lineas": numero(c[6]), "horas_sin": numero(c[7]), "horas_con": numero(c[8]), "coste": numero(c[9]),
            }
    fichas, actual = {}, None
    for linea in texto.split("\n"):
        m = re.match(r"^### HU-(\d+) · ", linea)
        if m:
            actual = int(m.group(1))
            fichas[actual] = 0
        elif linea.startswith("## "):
            actual = None
        elif actual is not None and re.match(r"^\| CA-\d+\.\d+ \|", linea):
            fichas[actual] += 1
    return resumen, fichas


def contar_pruebas():
    metodos, clases, por_clase = 0, 0, {}
    for fichero in sorted(PRUEBAS.rglob("*.java")):
        codigo = fichero.read_text(encoding="utf-8")
        n = len(re.findall(r"@(Test|ParameterizedTest|RepeatedTest|TestFactory)\b", codigo))
        por_clase[fichero.stem] = set(re.findall(r"void (\w+)\s*\(", codigo))
        if n:
            metodos += n
            clases += 1
    return metodos, clases, por_clase


def problemas_de_trazabilidad(por_clase):
    errores = []
    for linea in TRAZABILIDAD.read_text(encoding="utf-8").split("\n"):
        if not (linea.startswith("| CA-") or linea.startswith("| RNF-")):
            continue
        criterio = linea.split("|")[1].strip()
        for clase, metodos in re.findall(r"\[`(\w+)`\]\([^)]*\)((?:`\.\w+`(?:, `\.\w+`)*)?)", linea):
            if clase not in por_clase:
                errores.append(f"{criterio}: no existe la clase de prueba {clase}")
                continue
            for metodo in re.findall(r"\.(\w+)`", metodos):
                if metodo not in por_clase[clase]:
                    errores.append(f"{criterio}: no existe {clase}.{metodo}")
    return errores


def fmt(valor, decimales=0):
    texto = f"{valor:,.{decimales}f}".replace(",", "X").replace(".", ",").replace("X", ".")
    return texto


def calcular():
    resumen, fichas = leer_historias()
    errores = []
    for n, datos in resumen.items():
        if fichas.get(n) != datos["criterios"]:
            errores.append(f"HU-{n}: el resumen dice {datos['criterios']} criterios y la ficha tiene {fichas.get(n)}")
    totales = {g: {"historias": 0, "criterios": 0, "puntos": 0, "lineas": 0.0, "horas_sin": 0.0, "horas_con": 0.0,
                   "coste": 0.0, "sin_estimar": []} for g in GRUPOS + ["Por hacer"]}
    for n, d in sorted(resumen.items()):
        t = totales[grupo(d["estado"])]
        t["historias"] += 1
        t["criterios"] += d["criterios"]
        if d["puntos"].isdigit():
            t["puntos"] += int(d["puntos"])
        elif not d["estado"].startswith("Descartada (27/11)"):
            t["sin_estimar"].append(n)
        for clave in ("lineas", "horas_sin", "horas_con", "coste"):
            t[clave] += d[clave]
    metodos, clases, por_clase = contar_pruebas()
    errores += problemas_de_trazabilidad(por_clase)
    return resumen, totales, metodos, clases, errores


def markdown(resumen, totales, metodos, clases):
    huecos = [n for n, d in resumen.items() if d["estado"].startswith("Descartada (27/11)")]
    reales = len(resumen) - len(huecos)
    criterios = sum(t["criterios"] for t in totales.values())
    filas = []
    for g in GRUPOS + ["Por hacer"]:
        t = totales[g]
        if not t["historias"]:
            continue
        sin = f" (sin estimar: {len(t['sin_estimar'])})" if t["sin_estimar"] else ""
        filas.append(f"| {g} | {t['historias']}{sin} | {t['criterios']} | {t['puntos']} | {fmt(t['lineas'])} | "
                     f"{fmt(t['horas_sin'])} | {fmt(t['horas_con'])} | {fmt(t['coste'])} $ |")
    return "\n".join([
        "# Totales del proyecto",
        "",
        "> Generado con `python3 scripts/totales.py` a partir de [historias-de-usuario.md](historias-de-usuario.md) y de "
        "las pruebas de `src/test/java`. No se edita a mano: la CI comprueba que está al día. Los demás documentos "
        "citan estas cifras.",
        "",
        f"- **Historias:** {reales} ({len(resumen)} números; {' y '.join(f'HU-{n}' for n in huecos)} son huecos de "
        f"historias descartadas porque no lo eran).",
        f"- **Criterios de aceptación:** {criterios}.",
        f"- **Pruebas automáticas:** {metodos} en {clases} clases.",
        "",
        "| Estado | Historias | Criterios | Puntos | Líneas | Horas sin IA | Horas con IA | Coste de la IA |",
        "|---|---:|---:|---:|---:|---:|---:|---:|",
        *filas,
        "",
        "Las descartadas Won't conservan sus puntos, pero no tienen líneas, horas ni coste. Las líneas de cada historia "
        "son estimadas a partir de sus puntos.",
        "",
    ])


def main():
    resumen, totales, metodos, clases, errores = calcular()
    contenido = markdown(resumen, totales, metodos, clases)
    if "--comprobar" in sys.argv:
        if not SALIDA.exists() or SALIDA.read_text(encoding="utf-8") != contenido:
            errores.append("docs/requisitos/totales.md no está al día: ejecuta python3 scripts/totales.py")
        for e in errores:
            print("ERROR:", e)
        if errores:
            sys.exit(1)
        print("Totales al día y trazabilidad correcta.")
        return
    SALIDA.write_text(contenido, encoding="utf-8")
    print(contenido)
    for e in errores:
        print("AVISO:", e)


if __name__ == "__main__":
    main()
