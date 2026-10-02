package es.ucm.fdi.is1.apuestas.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Equipo con su plantilla tal como lo devuelve la API v4 de football-data.org
 * ({@code GET /competitions/{codigo}/teams}). Solo se leen los campos que usamos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlantillaApi(Integer id, String name, String shortName, List<JugadorApi> squad) {

    /** {@code position}: "Goalkeeper", "Defence", "Midfield", "Offence" o una más detallada. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JugadorApi(Integer id, String name, String position, String dateOfBirth, String nationality,
                             Integer shirtNumber) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(List<PlantillaApi> teams) {
    }
}
