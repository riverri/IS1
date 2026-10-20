package es.ucm.fdi.is1.apuestas.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Equipo con su plantilla tal como lo devuelve la API v4 de football-data.org
 * ({@code GET /competitions/{codigo}/teams}). Solo se leen los campos que usamos.
 * Lo usa {@link SincronizacionPlantillas} (HU-50).
 *
 * @param id        identificador del equipo en la API (el mismo que en {@link PartidoApi.EquipoApi})
 * @param name      nombre largo
 * @param shortName nombre corto
 * @param squad     jugadores; vacía en las competiciones que no traen plantillas en el plan gratuito
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlantillaApi(Integer id, String name, String shortName, List<JugadorApi> squad) {

    /**
     * {@code position}: "Goalkeeper", "Defence", "Midfield", "Offence" o una más detallada.
     * {@code id} se guarda en el jugador como {@code idExterno}; {@code dateOfBirth} empieza por AAAA-MM-DD.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JugadorApi(Integer id, String name, String position, String dateOfBirth, String nationality,
                             Integer shirtNumber) {
    }

    /** Respuesta completa de la API: la lista de equipos viene en el campo {@code teams}. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(List<PlantillaApi> teams) {
    }
}
