package es.ucm.fdi.is1.apuestas.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Comprueba la petición a football-data.org y la lectura de su respuesta (formato de la API v4). */
class FootballDataClienteTest {

    private static final String RESPUESTA = """
            {
              "filters": {"dateFrom": "2026-10-01", "dateTo": "2026-10-20"},
              "resultSet": {"count": 2},
              "competition": {"id": 2014, "name": "Primera Division", "code": "PD"},
              "matches": [
                {
                  "id": 500001,
                  "utcDate": "2026-10-10T16:30:00Z",
                  "status": "TIMED",
                  "matchday": 8,
                  "stage": "REGULAR_SEASON",
                  "homeTeam": {"id": 81, "name": "FC Barcelona", "shortName": "Barça", "tla": "FCB",
                               "crest": "https://crests.football-data.org/81.png"},
                  "awayTeam": {"id": 82, "name": "Getafe CF", "shortName": "Getafe", "tla": "GET",
                               "crest": "https://crests.football-data.org/82.png"},
                  "score": {"winner": null, "duration": "REGULAR", "fullTime": {"home": null, "away": null}}
                },
                {
                  "id": 500002,
                  "utcDate": "2026-09-20T19:00:00Z",
                  "status": "FINISHED",
                  "matchday": 7,
                  "homeTeam": {"id": 78, "name": "Club Atlético de Madrid", "shortName": "Atleti"},
                  "awayTeam": {"id": 86, "name": "Real Madrid CF", "shortName": "Real Madrid"},
                  "score": {"winner": "HOME_TEAM", "fullTime": {"home": 2, "away": 1}}
                }
              ]
            }
            """;

    @Test
    void pideLosPartidosConLaClaveYLeeLaRespuesta() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        ApiProperties propiedades = new ApiProperties("https://api.football-data.org/v4", "mi-clave",
                List.of("PD"), 7, 21, "Europe/Madrid");
        FootballDataCliente cliente = new FootballDataCliente(builder, propiedades);

        servidor.expect(requestTo(
                        "https://api.football-data.org/v4/competitions/PD/matches?dateFrom=2026-10-01&dateTo=2026-10-20"))
                .andExpect(header("X-Auth-Token", "mi-clave"))
                .andRespond(withSuccess(RESPUESTA, MediaType.APPLICATION_JSON));

        List<PartidoApi> partidos = cliente.partidos("PD", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 20));

        servidor.verify();
        assertThat(partidos).hasSize(2);
        PartidoApi primero = partidos.get(0);
        assertThat(primero.id()).isEqualTo(500001L);
        assertThat(primero.status()).isEqualTo("TIMED");
        assertThat(primero.matchday()).isEqualTo(8);
        assertThat(primero.homeTeam().crest()).isEqualTo("https://crests.football-data.org/81.png");
        assertThat(primero.score().winner()).isNull();
        assertThat(partidos.get(1).score().winner()).isEqualTo("HOME_TEAM");
    }
}
