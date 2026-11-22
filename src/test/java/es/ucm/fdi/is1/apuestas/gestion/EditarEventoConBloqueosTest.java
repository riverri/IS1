package es.ucm.fdi.is1.apuestas.gestion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaService;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Sexta depuración: no se cambian los equipos de un partido con bloqueos de liga pagados. */
@SpringBootTest
@Transactional
class EditarEventoConBloqueosTest {
    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarios;
    @Autowired CompeticionRepository competiciones;
    @Autowired EquipoRepository equipos;
    @Autowired EventoRepository eventos;
    @Autowired LigaService ligas;
    @Autowired BloqueoLigaService bloqueos;
    @Autowired GestionService gestion;

    @Test
    void noSeCambianLosEquiposDeUnPartidoConBloqueosPagados() {
        usuarioService.crear("ana6@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear("luis6@ucm.es", "Luis", "secreta123", Rol.USUARIO);
        LocalDateTime manana = Hora.ahora().plusDays(1);
        Evento e = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(), manana));
        Liga liga = ligas.crear("ana6@ucm.es", "Amigos");
        ligas.unirse("luis6@ucm.es", liga.getCodigo());
        bloqueos.crear(liga.getId(), "ana6@ucm.es", usuarios.findByEmail("luis6@ucm.es").orElseThrow().getId(), e.getId(), "LOCAL");
        EdicionEventoForm form = new EdicionEventoForm();
        form.setLocalId(equipos.findByNombre("Real Madrid").orElseThrow().getId());
        form.setVisitanteId(e.getVisitante().getId());
        form.setFechaHora(manana);
        // Ana pagó 50 por impedir a Luis apostar al Getafe; ahora le impediría apostar al Real Madrid
        assertThatThrownBy(() -> gestion.editarEvento(e.getId(), form)).isInstanceOf(DatosInvalidosException.class).hasMessageContaining("bloqueos de liga");
    }
}
