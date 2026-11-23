package es.ucm.fdi.is1.apuestas.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquiposService;
import es.ucm.fdi.is1.apuestas.eventos.CatalogoService;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class RevisionOptimizacionTest {

    @Autowired private EntityManager em;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private EventoRepository eventos;
    @Autowired private CatalogoService catalogo;
    @Autowired private EquiposService listadoEquipos;
    @Autowired private LigaService ligas;
    @Autowired private UsuarioService usuarios;

    @Test
    void laBusquedaSigueIgnorandoTodasLasTildes() {
        var liga = competiciones.findByNombre("LaLiga").orElseThrow();
        Equipo koln = equipos.save(new Equipo("1. FC Köln revisión", liga.getDeporte(), 6.0));
        Equipo barca = equipos.save(new Equipo("Barça revisión", liga.getDeporte(), 6.0));
        eventos.save(new Evento(liga, koln, barca, Hora.ahora().plusDays(2)));
        em.flush();
        em.clear();
        // Antes (Nombres.normalizar en Java) los cuatro encontraban el partido / el equipo
        assertThat(catalogo.buscar(null, "koln revision")).as("antes: Java").isNotEmpty();
        var soft = new org.assertj.core.api.SoftAssertions();
        soft.assertThat(catalogo.buscar(null, "koln revision", 0).eventos()).as("catalogo koln").isNotEmpty();
        soft.assertThat(catalogo.buscar(null, "Köln revisión", 0).eventos()).as("catálogo 'Köln'").isNotEmpty();
        soft.assertThat(listadoEquipos.buscar(null, null, "barca revision", 0).equipos()).as("equipos 'barca'")
                .isNotEmpty();
        soft.assertThat(listadoEquipos.buscar(null, null, "Barça revisión", 0).equipos()).as("equipos Barca-cedilla")
                .isNotEmpty();
        soft.assertAll();
    }

    @Test
    void misLigasCargaTodosLosMiembros() {
        usuarios.crear("rev-a@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        usuarios.crear("rev-b@ucm.es", "Bea", "secreta123", Rol.USUARIO);
        usuarios.crear("rev-c@ucm.es", "Carla", "secreta123", Rol.USUARIO);
        Liga liga = ligas.crear("rev-a@ucm.es", "Liga revisión");
        ligas.unirse("rev-b@ucm.es", liga.getCodigo());
        ligas.unirse("rev-c@ucm.es", liga.getCodigo());
        em.flush();
        em.clear();
        List<Liga> mias = ligas.mias("rev-a@ucm.es");
        assertThat(mias).hasSize(1);
        assertThat(mias.getFirst().getMiembrosActivos()).hasSize(3);
        em.clear();
        assertThat(ligas.clasificacion("rev-a@ucm.es").getFirst().miembros()).isEqualTo(3);
    }
}
