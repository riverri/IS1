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
    public record MarcadorApi(String winner, String duration, GolesApi fullTime) {

        public MarcadorApi(String winner) {
            this(winner, null, null);
        }

        /**
         * El marcador sirve para las apuestas de goles (HU-52) solo si el partido acabó en los 90 minutos:
         * con prórroga o penaltis, {@code fullTime} incluye esos goles.
         */
        public boolean isMarcadorValido() {
            return (duration == null || "REGULAR".equals(duration))
                    && fullTime != null && fullTime.home() != null && fullTime.away() != null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GolesApi(Integer home, Integer away) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(List<PartidoApi> matches) {
    }
}
