package es.ucm.fdi.is1.apuestas.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.JugadorRepository;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;

/** Descarga de plantillas reales desde la API (simulada). */
@SpringBootTest(properties = {"apuestas.api.token=clave-de-prueba", "apuestas.api.retraso-inicial=PT1H",
        "apuestas.api.retraso-inicial-plantillas=PT1H"})
@Transactional
class SincronizacionPlantillasTest {

    @MockitoBean
    private FuenteDatosDeportivos fuente;

    @Autowired
    private SincronizacionPlantillas sincronizacion;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private JugadorRepository jugadores;

    private Equipo getafe;

    @BeforeEach
    void preparar() {
        getafe = equipos.findByNombre("Getafe CF").orElseThrow();
        when(fuente.plantillas(eq("CL"))).thenReturn(List.of());
    }

    private static PlantillaApi.JugadorApi jugador(int id, String nombre, String posicion, Integer dorsal) {
        return new PlantillaApi.JugadorApi(id, nombre, posicion, "2000-01-15", "Spain", dorsal);
    }

    private void devuelve(PlantillaApi... plantillas) {
        when(fuente.plantillas(eq("PD"))).thenReturn(List.of(plantillas));
    }

    @Test
    void creaLosJugadoresDeLosEquiposQueYaTenemos() {
        devuelve(new PlantillaApi(getafe.getIdExterno(), "Getafe CF", "Getafe", List.of(
                        jugador(9001, "Portero Uno", "Goalkeeper", 13),
                        jugador(9002, "Defensa Dos", "Centre-Back", 4),
                        new PlantillaApi.JugadorApi(9003, "Sin posición", null, null, null, null))),
                new PlantillaApi(999999, "Equipo que no tenemos", "Otro", List.of(jugador(9100, "Nadie", "Midfield", 8))));

        SincronizacionPlantillas.Resumen resumen = sincronizacion.sincronizar();

        assertThat(resumen.equipos()).isEqualTo(1);
        assertThat(resumen.nuevos()).isEqualTo(2);
        Jugador portero = jugadores.findByIdExterno(9001).orElseThrow();
        assertThat(portero.getEquipo()).isEqualTo(getafe);
        assertThat(portero.getPosicion()).isEqualTo(Posicion.PORTERO);
        assertThat(portero.getDorsal()).isEqualTo(13);
        assertThat(portero.getNota()).isEqualTo(Jugador.NOTA_INICIAL);
        assertThat(portero.getEdad()).isNotNull();
        assertThat(jugadores.findByIdExterno(9100)).isEmpty();
    }

    @Test
    void actualizaSinTocarLaNotaYQuitaALosQueSeVan() {
        devuelve(new PlantillaApi(getafe.getIdExterno(), "Getafe CF", "Getafe", List.of(
                jugador(9001, "Portero Uno", "Goalkeeper", 13), jugador(9002, "Defensa Dos", "Defence", 4))));
        sincronizacion.sincronizar();
        jugadores.findByIdExterno(9001).orElseThrow().cambiarNota(8.4);
        Jugador manual = jugadores.save(new Jugador(getafe, "Canterano", Posicion.DELANTERO));

        devuelve(new PlantillaApi(getafe.getIdExterno(), "Getafe CF", "Getafe", List.of(
                jugador(9001, "Portero Uno", "Goalkeeper", 1))));
        SincronizacionPlantillas.Resumen resumen = sincronizacion.sincronizar();

        assertThat(resumen.actualizados()).isEqualTo(1);
        assertThat(resumen.retirados()).isEqualTo(1);
        Jugador portero = jugadores.findByIdExterno(9001).orElseThrow();
        assertThat(portero.getDorsal()).isEqualTo(1);
        assertThat(portero.getNota()).isEqualTo(8.4);
        assertThat(jugadores.findByIdExterno(9002)).isEmpty();
        assertThat(jugadores.findById(manual.getId())).isPresent();
    }

    /** Un traspaso entre dos equipos nuestros: aunque su equipo anterior se procese antes, conserva su nota. */
    @Test
    void unTraspasoEntreDosEquiposNuestrosConservaLaNota() {
        Equipo sevilla = equipos.findByNombre("Sevilla FC").orElseThrow();
        devuelve(new PlantillaApi(sevilla.getIdExterno(), "Sevilla FC", "Sevilla", List.of(
                        jugador(9201, "Mediocentro Viajero", "Central Midfield", 8))),
                new PlantillaApi(getafe.getIdExterno(), "Getafe CF", "Getafe", List.of(
                        jugador(9202, "Portero Fijo", "Goalkeeper", 1))));
        sincronizacion.sincronizar();
        Jugador viajero = jugadores.findByIdExterno(9201).orElseThrow();
        viajero.cambiarNota(9.5);
        Long id = viajero.getId();

        // Ahora juega en el Getafe; el Sevilla sale primero en la lista de la API
        devuelve(new PlantillaApi(sevilla.getIdExterno(), "Sevilla FC", "Sevilla", List.of(
                        jugador(9203, "Otro del Sevilla", "Goalkeeper", 13))),
                new PlantillaApi(getafe.getIdExterno(), "Getafe CF", "Getafe", List.of(
                        jugador(9202, "Portero Fijo", "Goalkeeper", 1), jugador(9201, "Mediocentro Viajero", "Central Midfield", 6))));
        sincronizacion.sincronizar();

        Jugador traspasado = jugadores.findByIdExterno(9201).orElseThrow();
        assertThat(traspasado.getId()).isEqualTo(id);
        assertThat(traspasado.getEquipo()).isEqualTo(getafe);
        assertThat(traspasado.getNota()).isEqualTo(9.5);
    }

    @Test
    void lasPlantillasDeLosEquiposDeChampionsSalenDeSuLiga() {
        Equipo bayern = equipos.findByNombre("Bayern de Múnich").orElseThrow();
        devuelve();
        when(fuente.plantillas(eq("BL1"))).thenReturn(List.of(new PlantillaApi(bayern.getIdExterno(),
                "FC Bayern München", "Bayern", List.of(jugador(9201, "Portero Bávaro", "Goalkeeper", null)))));

        sincronizacion.sincronizar();

        Jugador portero = jugadores.findByIdExterno(9201).orElseThrow();
        assertThat(portero.getEquipo()).isEqualTo(bayern);
        assertThat(portero.getDorsal()).as("el plan gratuito no da dorsales").isNull();
    }

    @Test
    void unaPlantillaVaciaNoBorraLaQueYaHay() {
        devuelve(new PlantillaApi(getafe.getIdExterno(), "Getafe CF", "Getafe", List.of(
                jugador(9001, "Portero Uno", "Goalkeeper", 13))));
        sincronizacion.sincronizar();

        devuelve(new PlantillaApi(getafe.getIdExterno(), "Getafe CF", "Getafe", List.of()));
        SincronizacionPlantillas.Resumen resumen = sincronizacion.sincronizar();

        assertThat(resumen.retirados()).isZero();
        assertThat(jugadores.findByIdExterno(9001)).isPresent();
    }

    /** N-18: un equipo de la API que se llama como uno nuestro ya enlazado con otro identificador no es el nuestro. */
    @Test
    void noSeMezclaConUnEquipoYaEnlazadoAOtroDeLaApi() {
        devuelve(new PlantillaApi(999999, "Getafe CF", "Getafe", List.of(jugador(9301, "Impostor", "Goalkeeper", 1))));

        sincronizacion.sincronizar();

        assertThat(jugadores.findByIdExterno(9301)).isEmpty();
    }

    @Test
    void siFallaLaLigaDelEquipoNuevoElTraspasadoNoSeBorra() {
        Equipo sevilla = equipos.findByNombre("Sevilla FC").orElseThrow();
        Equipo destino = equipos.findByNombre("Getafe CF").orElseThrow();   // como si fuera de otra liga (PL)
        devuelve(new PlantillaApi(sevilla.getIdExterno(), "Sevilla FC", "Sevilla", List.of(
                jugador(9301, "Viajero", "Central Midfield", 8))));
        sincronizacion.sincronizar();
        Jugador viajero = jugadores.findByIdExterno(9301).orElseThrow();
        viajero.cambiarNota(9.5);
        Long id = viajero.getId();

        // Se va al otro equipo; esa liga da error 429 hoy
        devuelve(new PlantillaApi(sevilla.getIdExterno(), "Sevilla FC", "Sevilla", List.of(
                jugador(9302, "Otro", "Goalkeeper", 13))));
        when(fuente.plantillas(eq("PL"))).thenThrow(new org.springframework.web.client.RestClientException("429"));
        sincronizacion.sincronizar();
        // Mañana la liga responde
        when(fuente.plantillas(eq("PL"))).thenReturn(List.of(new PlantillaApi(destino.getIdExterno(), "Getafe CF",
                "Getafe", List.of(jugador(9301, "Viajero", "Central Midfield", 6)))));
        sincronizacion.sincronizar();

        Jugador traspasado = jugadores.findByIdExterno(9301).orElseThrow();
        assertThat(traspasado.getId()).as("es el mismo jugador").isEqualTo(id);
        assertThat(traspasado.getNota()).isEqualTo(9.5);
    }
}
