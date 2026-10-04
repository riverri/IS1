package es.ucm.fdi.is1.apuestas.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Partido tal como lo devuelve la API v4 de football-data.org
 * ({@code GET /competitions/{codigo}/matches}). Solo se leen los campos que usamos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PartidoApi(Long id, String utcDate, String status, Integer matchday, EquipoApi homeTeam,
                         EquipoApi awayTeam, MarcadorApi score) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EquipoApi(Integer id, String name, String shortName, String crest) {
    }

    /**
     * {@code winner}: HOME_TEAM, AWAY_TEAM, DRAW o null si no ha terminado.
     * {@code duration}: REGULAR, EXTRA_TIME o PENALTY_SHOOTOUT. {@code fullTime}: goles al final.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MarcadorApi(String winner, String duration, GolesApi fullTime, GolesApi regularTime) {

        public MarcadorApi(String winner) {
            this(winner, null, null, null);
        }

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
            return goles != null && goles.home() != null && goles.away() != null ? goles : null;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GolesApi(Integer home, Integer away) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(List<PartidoApi> matches) {
    }
}
