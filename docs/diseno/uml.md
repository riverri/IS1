# Diseño: diagramas UML

Diagramas del sistema tal como está implementado al final del Sprint 8. Están escritos en [Mermaid](https://mermaid.js.org/), que GitHub dibuja directamente al abrir este archivo. Para editarlos se puede usar el editor de <https://mermaid.live>.

1. [Casos de uso](#1-casos-de-uso)
2. [Arquitectura por capas](#2-arquitectura-por-capas)
3. [Modelo del dominio (clases)](#3-modelo-del-dominio-clases)
4. [Estados de un evento, una apuesta y un mercado](#4-estados)
5. [Secuencia: apostar una combinada](#5-secuencia-apostar-una-combinada)
6. [Secuencia: resolver un partido](#6-secuencia-resolver-un-partido)
7. [Modelo de datos](#7-modelo-de-datos)

## 1. Casos de uso

Mermaid no tiene diagrama de casos de uso; se representa con actores a los lados y los casos de uso en el centro. Entre paréntesis, la historia de usuario.

```mermaid
flowchart LR
    visitante(["👤 Visitante"])
    usuario(["👤 Usuario"])
    creador(["👤 Creador de apuestas"])
    api(["🌐 API football-data.org"])

    subgraph sistema [Web de apuestas]
        direction TB
        cu1(["Consultar catálogo y cuotas (HU-08, HU-19, HU-20, HU-22)"])
        cu2(["Consultar ficha de equipo y cara a cara (HU-31, HU-33)"])
        cu3(["Ver ranking y perfil de jugadores (HU-36, HU-48)"])
        cu4(["Registrarse e iniciar sesión (HU-11, HU-12)"])
        cu5(["Apostar simple o combinada (HU-23, HU-28, HU-29)"])
        cu6(["Apostar a largo plazo (HU-44)"])
        cu7(["Cancelar o cambiar el importe (HU-26, HU-27)"])
        cu8(["Ver mis apuestas, historial y estadísticas (HU-24, HU-34, HU-35)"])
        cu9(["Recibir avisos (HU-37)"])
        cu10(["Gestionar mi cuenta y saldo (HU-13, HU-14, HU-47)"])
        cu11(["Dar de alta competiciones, equipos y eventos (HU-01, HU-46)"])
        cu12(["Ajustar calificación y forma (HU-01, HU-02)"])
        cu13(["Introducir resultado, suspender o anular (HU-04, HU-05, HU-06)"])
        cu14(["Gestionar mercados a largo plazo (HU-45)"])
        cu15(["Fijar límites de apuesta (HU-07)"])
        cu16(["Sincronizar partidos y resultados (HU-21, HU-25)"])
    end

    visitante --- cu1 & cu2 & cu3 & cu4
    usuario --- cu5 & cu6 & cu7 & cu8 & cu9 & cu10
    creador --- cu11 & cu12 & cu13 & cu14 & cu15
    api --- cu16
```

El usuario hereda los casos del visitante, y el creador los del usuario salvo apostar (el creador no aparece en el ranking).

## 2. Arquitectura por capas

Aplicación web monolítica con Spring Boot (MVC). Cada paquete agrupa una parte del dominio con sus controladores, servicios, entidades y repositorios.

```mermaid
flowchart TB
    navegador["Navegador (HTML generado con Thymeleaf + CSS)"]

    subgraph presentacion [Presentación]
        controladores["Controladores Spring MVC<br/>EventosController · ApuestaController · BoletoController<br/>MercadosController · GestionController · UsuariosController…"]
        plantillas["Plantillas Thymeleaf<br/>templates/*.html"]
    end

    subgraph negocio [Lógica de negocio]
        servicios["Servicios<br/>ApuestaService · BoletoService · ResolucionService<br/>CalculadoraCuotas · RankingService · FichaEquipoService…"]
        dominio["Entidades del dominio<br/>Apuesta · Seleccion · Evento · Equipo · Mercado · Usuario…"]
    end

    subgraph datos [Acceso a datos]
        repositorios["Repositorios Spring Data JPA"]
        flyway["Migraciones Flyway<br/>V1 … V4"]
        h2[("Base de datos H2<br/>./datos/apuestas")]
    end

    seguridad["Spring Security<br/>login, roles USUARIO / CREADOR"]
    api["API football-data.org"]
    programada["SincronizacionProgramada<br/>cada 30 minutos"]

    navegador -->|HTTP| seguridad --> controladores
    controladores --> plantillas
    controladores --> servicios
    servicios --> dominio
    servicios --> repositorios --> h2
    flyway --> h2
    programada --> servicios
    servicios -->|RestClient| api
```

| Paquete | Contenido |
|---|---|
| `usuarios` | Registro, inicio de sesión, cuenta, saldo y recargas |
| `equipos` | Competiciones, equipos, deportes y forma |
| `eventos` | Eventos, catálogo, ficha de equipo y cara a cara |
| `cuotas` | Algoritmo de cuotas (calidad, forma y volumen) |
| `apuesta` | Apuestas, selecciones, boleto, resolución, límites, ranking y avisos de apuestas |
| `mercados` | Mercados a largo plazo y sus candidatos |
| `notificaciones` | Avisos a los usuarios |
| `gestion` | Panel del creador de apuestas |
| `api` | Cliente de football-data.org y sincronización |
| `config`, `web` | Seguridad, datos iniciales, reloj y utilidades de las vistas |

## 3. Modelo del dominio (clases)

Atributos y operaciones principales; se omiten los getters y los constructores.

```mermaid
classDiagram
    direction LR

    class Usuario {
        -String email
        -String nombre
        -String passwordHash
        -Rol rol
        -BigDecimal saldo
        -LocalDateTime ultimaRecarga
        +cargar(importe)
        +abonar(importe)
        +ajustar(diferencia)
        +aplicarRecargaPeriodica(ahora, periodo, importe) bool
        +cambiarNombre(nombre)
    }

    class Competicion {
        -String nombre
        -Deporte deporte
    }

    class Equipo {
        -String nombre
        -Deporte deporte
        -Double calidad
        -Forma forma
        -String escudoUrl
        -Integer idExterno
        +participaEn(competicionId) bool
    }

    class Evento {
        -LocalDateTime fechaHora
        -EstadoEvento estado
        -Resultado resultado
        -String fase
        -Long idExterno
        +admiteApuestas(ahora) bool
        +finalizar(resultado)
        +suspender()
        +reactivar()
        +anular()
        +modificar(local, visitante, fecha, fase, ahora)
    }

    class Apuesta {
        -BigDecimal importe
        -BigDecimal cuota
        -EstadoApuesta estado
        -BigDecimal pagado
        -LocalDateTime fecha
        +anadir(evento, pronostico, cuota)
        +anadir(candidato, cuota)
        +resolver(evento, resultado)
        +resolver(mercado, ganador)
        +anular(evento)
        +cancelar(ahora)
        +cambiarImporte(nuevo, cuotas, ahora)
        +getGananciaPotencial() BigDecimal
    }

    class Seleccion {
        -Resultado pronostico
        -BigDecimal cuota
        -EstadoSeleccion estado
        +getCuotaEfectiva() BigDecimal
        +isLargoPlazo() bool
    }

    class Mercado {
        -String nombre
        -Deporte deporte
        -LocalDateTime cierre
        -EstadoMercado estado
        +anadirCandidato(nombre, cuota) Candidato
        +admiteApuestas(ahora) bool
        +cerrar()
        +resolver(ganador)
        +anular()
    }

    class Candidato {
        -String nombre
        -BigDecimal cuota
        +cambiarCuota(cuota)
    }

    class Limites {
        -BigDecimal importeMinimo
        -BigDecimal importeMaximo
        -int maxSelecciones
        +comprobarImporte(importe)
        +comprobarSelecciones(n)
    }

    class Notificacion {
        -String texto
        -String tipo
        -LocalDateTime fecha
        -boolean leida
        +marcarLeida()
    }

    class CalculadoraCuotas {
        <<service>>
        +calcular(evento) Cuotas
    }

    class Cuotas {
        <<record>>
        BigDecimal local
        BigDecimal empate
        BigDecimal visitante
    }

    class Boleto {
        <<sesión>>
        +anadir(eventoId, resultado, cuota)
        +quitar(eventoId)
    }

    Usuario "1" --> "*" Apuesta : hace
    Usuario "1" --> "*" Notificacion : recibe
    Apuesta "1" *-- "1..*" Seleccion : selecciones
    Seleccion "*" --> "0..1" Evento : pronóstico sobre
    Seleccion "*" --> "0..1" Candidato : o candidato
    Evento "*" --> "1" Competicion
    Evento "*" --> "1" Equipo : local
    Evento "*" --> "1" Equipo : visitante
    Equipo "*" --> "*" Competicion : participa en
    Mercado "1" *-- "2..*" Candidato : candidatos
    Mercado "*" --> "0..1" Candidato : ganador
    Candidato "*" --> "0..1" Equipo : es
    CalculadoraCuotas ..> Evento : usa calidad y forma
    CalculadoraCuotas ..> Cuotas : crea
    Boleto ..> Apuesta : al confirmar crea
```

**Enumerados:** `Deporte` (FUTBOL, BALONCESTO, TENIS, AUTOMOVILISMO, MOTOCICLISMO), `Forma` (MUY_MALA … MUY_BUENA), `Rol` (USUARIO, CREADOR), `Resultado` (LOCAL, EMPATE, VISITANTE), `EstadoEvento`, `EstadoApuesta`, `EstadoSeleccion` y `EstadoMercado`.

**Decisiones de diseño:**
- **Una apuesta simple es una combinada de una sola selección,** así hay una única forma de guardar, pagar y mostrar las apuestas.
- **Una selección apunta a un evento (con su pronóstico) o a un candidato de un mercado,** nunca a los dos. Por eso las apuestas a largo plazo comparten historial, estadísticas y ranking con el resto.
- **La cuota se guarda en cada selección al apostar:** los cambios posteriores (forma, dinero apostado, cuotas del creador) no afectan a las apuestas ya hechas.
- **`pagado` guarda lo que ya se ha abonado,** así una corrección de resultado paga o retira solo la diferencia.

## 4. Estados

### Evento

```mermaid
stateDiagram-v2
    [*] --> PROGRAMADO : alta (creador o API)
    PROGRAMADO --> SUSPENDIDO : suspender / aplazado en la API
    SUSPENDIDO --> PROGRAMADO : reactivar
    PROGRAMADO --> FINALIZADO : introducir resultado
    SUSPENDIDO --> FINALIZADO : introducir resultado
    FINALIZADO --> FINALIZADO : corregir resultado
    PROGRAMADO --> ANULADO : anular (se devuelve el importe)
    SUSPENDIDO --> ANULADO : anular
    PROGRAMADO --> [*] : borrar (solo sin apuestas)
    FINALIZADO --> [*]
    ANULADO --> [*]
```

### Apuesta

```mermaid
stateDiagram-v2
    [*] --> ACTIVA : apostar (se descuenta el importe)
    ACTIVA --> ACTIVA : cambiar importe / se acierta una selección de la combinada
    ACTIVA --> CANCELADA : cancelar antes de empezar (se devuelve)
    ACTIVA --> GANADA : todas las selecciones acertadas o anuladas
    ACTIVA --> PERDIDA : falla una selección
    ACTIVA --> ANULADA : se anulan todas (se devuelve)
    GANADA --> PERDIDA : corrección de resultado
    PERDIDA --> GANADA : corrección de resultado
    GANADA --> [*]
    PERDIDA --> [*]
    ANULADA --> [*]
    CANCELADA --> [*]
```

Cada paso a GANADA, PERDIDA o ANULADA crea un aviso para el usuario (HU-37).

### Mercado a largo plazo

```mermaid
stateDiagram-v2
    [*] --> ABIERTO : el creador lo da de alta
    ABIERTO --> CERRADO : cerrar antes de la fecha
    ABIERTO --> RESUELTO : marcar ganador
    CERRADO --> RESUELTO : marcar ganador
    RESUELTO --> RESUELTO : corregir ganador
    ABIERTO --> ANULADO : anular (se devuelve el importe)
    CERRADO --> ANULADO : anular
    RESUELTO --> [*]
    ANULADO --> [*]
```

Un mercado abierto deja de admitir apuestas al llegar su fecha de cierre aunque siga en ABIERTO.

## 5. Secuencia: apostar una combinada

El usuario ya ha añadido varias cuotas del catálogo a su boleto y pulsa *Confirmar apuesta* (HU-28, HU-29).

```mermaid
sequenceDiagram
    actor U as Usuario
    participant BC as BoletoController
    participant BS as BoletoService
    participant AS as ApuestaService
    participant L as Limites
    participant CC as CalculadoraCuotas
    participant A as Apuesta
    participant US as Usuario (entidad)
    participant AR as ApuestaRepository

    U->>BC: POST /boleto/confirmar (importe)
    BC->>BS: confirmar(email, boleto, importe)
    BS->>AS: apostar(email, selecciones con la cuota vista, importe)
    AS->>L: comprobarSelecciones(n) y comprobarImporte(importe)
    loop cada selección
        AS->>AS: buscar el evento y comprobar que admite apuestas
        AS->>CC: calcular(evento)
        CC-->>AS: cuotas actuales
        AS->>A: anadir(evento, pronóstico, cuota)
    end
    alt alguna cuota distinta de la vista
        AS-->>BS: CuotasCambiadasException
        BS->>BS: actualizar las cuotas del boleto
        BS-->>BC: excepción
        BC-->>U: aviso "han cambiado las cuotas", confirmar de nuevo
    else cuotas iguales
        AS->>US: cargar(importe)
        AS->>AR: save(apuesta)
        AS-->>BS: apuesta
        BS->>BS: vaciar el boleto
        BC-->>U: redirige a Mis apuestas con la ganancia potencial
    end
```

## 6. Secuencia: resolver un partido

El resultado lo introduce el creador en *Gestión* o llega de la API en la sincronización programada (HU-04, HU-25, HU-37).

```mermaid
sequenceDiagram
    actor C as Creador / sincronización
    participant RS as ResolucionService
    participant E as Evento
    participant SR as SeleccionRepository
    participant A as Apuesta
    participant U as Usuario (entidad)
    participant AV as AvisosApuestas
    participant NS as NotificacionService

    C->>RS: introducirResultado(eventoId, resultado)
    RS->>E: finalizar(resultado)
    RS->>SR: findByEvento(evento)
    SR-->>RS: selecciones del evento
    loop cada selección
        RS->>A: resolver(evento, resultado)
        A->>A: marcar la selección acertada o fallada y recalcular el estado
        A->>U: ajustar(lo que debe cobrar − lo ya pagado)
        RS->>AV: siCambia(apuesta, estado anterior)
        opt ganada, perdida o anulada
            AV->>NS: avisar(usuario, tipo, texto)
        end
    end
    RS-->>C: número de apuestas resueltas
```

## 7. Modelo de datos

Tablas que crean las migraciones de `src/main/resources/db/migration`.

```mermaid
erDiagram
    USUARIO ||--o{ APUESTA : hace
    USUARIO ||--o{ NOTIFICACION : recibe
    APUESTA ||--|{ SELECCION : contiene
    EVENTO ||--o{ SELECCION : "pronóstico sobre"
    CANDIDATO ||--o{ SELECCION : "apuesta a"
    COMPETICION ||--o{ EVENTO : organiza
    EQUIPO ||--o{ EVENTO : "juega como local o visitante"
    EQUIPO }o--o{ COMPETICION : "equipo_competiciones"
    MERCADO ||--|{ CANDIDATO : tiene
    EQUIPO |o--o{ CANDIDATO : "es"

    USUARIO {
        bigint id PK
        varchar email UK
        varchar nombre
        varchar password_hash
        enum rol
        numeric saldo
        timestamp ultima_recarga
    }
    APUESTA {
        bigint id PK
        bigint usuario_id FK
        numeric importe
        numeric cuota
        enum estado
        numeric pagado
        timestamp fecha
    }
    SELECCION {
        bigint id PK
        bigint apuesta_id FK
        bigint evento_id FK "nulo en largo plazo"
        enum pronostico "nulo en largo plazo"
        bigint candidato_id FK "nulo en partidos"
        numeric cuota
        enum estado
    }
    EVENTO {
        bigint id PK
        bigint competicion_id FK
        bigint local_id FK
        bigint visitante_id FK
        timestamp fecha_hora
        enum estado
        enum resultado
        varchar fase
        bigint id_externo UK
    }
    EQUIPO {
        bigint id PK
        varchar nombre
        enum deporte
        float calidad
        enum forma
        varchar escudo_url
        integer id_externo UK
    }
    COMPETICION {
        bigint id PK
        varchar nombre
        enum deporte
    }
    MERCADO {
        bigint id PK
        varchar nombre
        enum deporte
        timestamp cierre
        enum estado
        bigint ganador_id FK
    }
    CANDIDATO {
        bigint id PK
        bigint mercado_id FK
        varchar nombre
        numeric cuota
        bigint equipo_id FK
    }
    NOTIFICACION {
        bigint id PK
        bigint usuario_id FK
        varchar texto
        varchar tipo
        timestamp fecha
        boolean leida
    }
    LIMITES {
        bigint id PK
        numeric importe_minimo
        numeric importe_maximo
        integer max_selecciones
    }
```
