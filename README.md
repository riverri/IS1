# IS1 · Web de apuestas deportivas

Proyecto de **Ingeniería del Software I** (doble grado Informática–Matemáticas, UCM, curso 2026/27).
Profesor: Gonzalo Rubén Méndez Pozo.

## Equipo

| Miembro | Rol Scrum (provisional) |
|---|---|
| Carlos Martín-Salas | _por decidir_ |
| Jaime Martín | _por decidir_ |
| David Ortega | _por decidir_ |
| Jing Li | _por decidir_ |
| Carlos Jurado | _por decidir_ |

> Los roles se definen en la primera reunión. Ver [docs/scrum/proceso.md](docs/scrum/proceso.md).

## ¿Qué es?

Una web donde los usuarios apuestan **sin dinero real** (moneda ficticia) sobre eventos deportivos:

- Apuestas simples y **combinadas**, con cálculo automático de la cuota/multiplicador, como en las casas de apuestas online.
- **Banco de estadísticas** para consultar la evolución de los equipos antes de apostar.

Más detalle en [docs/requisitos/vision.md](docs/requisitos/vision.md).

## Estructura del repositorio

```
.
├── docs/
│   ├── requisitos/      # Visión del producto, requisitos
│   ├── scrum/           # Proceso, Product Backlog y actas de sprint
│   └── decisiones/      # Decisiones técnicas (stack, arquitectura…)
├── src/                 # Código fuente (cuando se elija el stack)
├── .github/             # Plantillas de issues y pull requests
└── CONTRIBUTING.md      # Cómo trabajamos con Git y GitHub
```

## Cómo empezar

1. Leer [CONTRIBUTING.md](CONTRIBUTING.md) (flujo Git, ramas y pull requests).
2. Leer [docs/scrum/proceso.md](docs/scrum/proceso.md) (cómo aplicamos Scrum).
3. Revisar el [Product Backlog](docs/scrum/product-backlog.md) inicial.
4. Decidir el stack: [docs/decisiones/0001-stack-tecnologico.md](docs/decisiones/0001-stack-tecnologico.md).

## Uso de IA

La asignatura permite usar IA. Todo lo generado con IA lo revisa una persona del equipo antes de integrarlo, y el equipo responde de ello.
Si un PR incluye contenido generado con IA, se indica en su descripción.
