package es.ucm.fdi.is1.apuestas.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

@SpringBootTest
@Transactional
class DatosInicialesTest {

    @Autowired
    private DatosIniciales datosIniciales;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private UsuarioRepository usuarios;

    @Test
    void cargaLosVeinteEquiposDeLaLiga() {
        long equiposLaLiga = equipos.findAll().stream()
                .filter(e -> e.getCompeticiones().stream().anyMatch(c -> c.getNombre().equals("LaLiga")))
                .count();
        assertThat(equiposLaLiga).isEqualTo(20);
    }

    @Test
    void volverAEjecutarlosNoDuplicaNada() {
        long antesCompeticiones = competiciones.count();
        long antesEquipos = equipos.count();
        long antesEventos = eventos.count();
        long antesUsuarios = usuarios.count();

        datosIniciales.run(new DefaultApplicationArguments());

        assertThat(competiciones.count()).isEqualTo(antesCompeticiones);
        assertThat(equipos.count()).isEqualTo(antesEquipos);
        assertThat(eventos.count()).isEqualTo(antesEventos);
        assertThat(usuarios.count()).isEqualTo(antesUsuarios);
    }
}
