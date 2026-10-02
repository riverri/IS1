package es.ucm.fdi.is1.apuestas.equipos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Alineación probable a partir de la plantilla y de la nota de cada jugador. */
class AlineacionTest {

    private final Equipo equipo = new Equipo("Equipo de prueba", Deporte.FUTBOL, 7.0);

    private Jugador jugador(String nombre, Posicion posicion, Integer dorsal, double nota) {
        Jugador jugador = new Jugador(equipo, nombre, posicion);
        jugador.setDorsal(dorsal);
        jugador.cambiarNota(nota);
        return jugador;
    }

    private List<Jugador> plantillaCompleta() {
        List<Jugador> plantilla = new ArrayList<>();
        plantilla.add(jugador("Portero titular", Posicion.PORTERO, 1, 7.5));
        plantilla.add(jugador("Portero suplente", Posicion.PORTERO, 13, 6.0));
        for (int i = 2; i <= 6; i++) {
            plantilla.add(jugador("Defensa " + i, Posicion.DEFENSA, i, 6.0));
        }
        for (int i = 7; i <= 10; i++) {
            plantilla.add(jugador("Medio " + i, Posicion.CENTROCAMPISTA, i, 6.0));
        }
        for (int i = 11; i <= 14; i++) {
            plantilla.add(jugador("Delantero " + i, Posicion.DELANTERO, i + 10, 6.0));
        }
        return plantilla;
    }

    @Test
    void sacaUn433ConLosDeMejorNota() {
        List<Jugador> plantilla = plantillaCompleta();
        plantilla.add(jugador("Crack", Posicion.DELANTERO, 30, 9.5));

        Alineacion alineacion = Alineacion.de(plantilla);

        assertThat(alineacion.sistema()).isEqualTo("4-3-3");
        assertThat(alineacion.lineas()).extracting(Alineacion.Linea::posicion).containsExactly(
                Posicion.DELANTERO, Posicion.CENTROCAMPISTA, Posicion.DEFENSA, Posicion.PORTERO);
        assertThat(alineacion.lineas()).extracting(l -> l.jugadores().size()).containsExactly(3, 3, 4, 1);
        assertThat(alineacion.lineas().get(0).jugadores()).extracting(Jugador::getNombre).contains("Crack");
        assertThat(alineacion.lineas().get(3).jugadores().get(0).getNombre()).isEqualTo("Portero titular");
    }

    @Test
    void aIgualNotaSaleElDorsalMasBajo() {
        Alineacion alineacion = Alineacion.de(plantillaCompleta());

        assertThat(alineacion.lineas().get(2).jugadores()).extracting(Jugador::getDorsal).containsExactly(2, 3, 4, 5);
    }

    @Test
    void sinJugadoresSuficientesNoHayAlineacion() {
        List<Jugador> plantilla = plantillaCompleta();
        plantilla.removeIf(j -> j.getPosicion() == Posicion.PORTERO);

        assertThat(Alineacion.de(plantilla)).isNull();
    }

    @Test
    void laNotaVaDe0A10ConUnDecimal() {
        Jugador jugador = jugador("Alguien", Posicion.DEFENSA, 4, 7.26);
        assertThat(jugador.getNota()).isEqualTo(7.3);
        assertThatThrownBy(() -> jugador.cambiarNota(10.5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void traduceLasPosicionesDeLaApi() {
        assertThat(Posicion.desdeApi("Goalkeeper")).isEqualTo(Posicion.PORTERO);
        assertThat(Posicion.desdeApi("Defence")).isEqualTo(Posicion.DEFENSA);
        assertThat(Posicion.desdeApi("Left-Back")).isEqualTo(Posicion.DEFENSA);
        assertThat(Posicion.desdeApi("Defensive Midfield")).isEqualTo(Posicion.CENTROCAMPISTA);
        assertThat(Posicion.desdeApi("Right Winger")).isEqualTo(Posicion.DELANTERO);
        assertThat(Posicion.desdeApi("Centre-Forward")).isEqualTo(Posicion.DELANTERO);
        assertThat(Posicion.desdeApi("Offence")).isEqualTo(Posicion.DELANTERO);
        assertThat(Posicion.desdeApi(null)).isNull();
    }
}
