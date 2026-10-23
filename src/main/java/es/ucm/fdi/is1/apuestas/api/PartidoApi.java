package es.ucm.fdi.is1.apuestas.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Partido tal como lo devuelve la API v4 de football-data.org
 * ({@code GET /competitions/{codigo}/matches}). Solo se leen los campos que usamos.
 * Con {@code ignoreUnknown} el resto del JSON se descarta sin dar error.
 * La librería Jackson rellena el record emparejando cada campo del JSON con el componente del mismo nombre
 * ({@code "utcDate": "..."} → {@code utcDate}); por eso los nombres están en inglés, como en la API.
 *
 * @param id       identificador del partido en la API; se guarda en el evento como {@code idExterno}
 * @param utcDate  fecha y hora de inicio en UTC, en formato ISO-8601 (p. ej. 2026-10-10T19:00:00Z)
 * @param status   estado: SCHEDULED, TIMED, IN_PLAY, PAUSED, FINISHED, POSTPONED, SUSPENDED, CANCELLED…
 * @param matchday número de jornada; puede ser null
 * @param homeTeam equipo local
 * @param awayTeam equipo visitante
 * @param score    ganador y marcador
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PartidoApi(Long id, String utcDate, String status, Integer matchday, EquipoApi homeTeam,
                         EquipoApi awayTeam, MarcadorApi score) {

    /**
     * Equipo de un partido: identificador en la API, nombre largo, nombre corto y dirección del escudo.
     * En una eliminatoria con el rival aún por decidir llega sin identificador.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EquipoApi(Integer id, String name, String shortName, String crest) {
    }

    /**
     * {@code winner}: HOME_TEAM, AWAY_TEAM, DRAW o null si no ha terminado.
     * {@code duration}: REGULAR, EXTRA_TIME o PENALTY_SHOOTOUT. {@code fullTime}: goles al final.
     * {@code regularTime}: goles de los 90 minutos, el que se usa si hubo prórroga.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MarcadorApi(String winner, String duration, GolesApi fullTime, GolesApi regularTime) {

        /** Solo con el ganador, sin marcador (se usa en las pruebas). */
        public MarcadorApi(String winner) {
            this(winner, null, null, null);
        }

        /** Sin {@code regularTime} (se usa en las pruebas). */
        public MarcadorApi(String winner, String duration, GolesApi fullTime) {
            this(winner, duration, fullTime, null);
        }

        /** Hubo prórroga o penaltis: el ganador y {@code fullTime} no son los de los 90 minutos. */
        public boolean isProrroga() {
            return duration != null && !"REGULAR".equals(duration);
        }

        /**
         * Marcador de los 90 minutos, que es el que deciden todas nuestras apuestas: {@code fullTime} si no hubo
         * prórroga, o {@code regularTime} si la hubo. Null si la API no lo da.
         */
        public GolesApi marcador90() {
            GolesApi goles = isProrroga() ? regularTime : fullTime;
            // Un marcador a medias (un lado null) no sirve para resolver apuestas
            return goles != null && goles.home() != null && goles.away() != null ? goles : null;
        }

    }

    /** Goles del local y del visitante; null mientras no se conocen. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GolesApi(Integer home, Integer away) {
    }

    /** Respuesta completa de la API: la lista de partidos viene en el campo {@code matches}. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(List<PartidoApi> matches) {
    }
}
