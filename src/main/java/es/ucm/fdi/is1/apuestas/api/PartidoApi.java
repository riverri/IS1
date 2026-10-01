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

    /** {@code winner}: HOME_TEAM, AWAY_TEAM, DRAW o null si no ha terminado. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MarcadorApi(String winner) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(List<PartidoApi> matches) {
    }
}
