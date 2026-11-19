package es.ucm.fdi.is1.apuestas.ligas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.CuotaHistoricaRepository;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionRepository;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * HU-89 (CA-89.2): aceptar un reto cuando el partido ya no admite apuestas lo anula y devuelve el importe al retador.
 * Sin @Transactional en la prueba: cada llamada va en su propia transacción, como en la aplicación real, para que se
 * vea si la devolución se guarda de verdad aunque el servicio avise con una excepción.
 */
@SpringBootTest
class RetoSinTransaccionTest {

    private static final String ANA = "ana-reto-caducado@ucm.es";
    private static final String LUIS = "luis-reto-caducado@ucm.es";

    @Autowired
    private RetoService retos;

    @Autowired
    private RetoRepository retoRepository;

    @Autowired
    private LigaService ligas;

    @Autowired
    private LigaRepository ligaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MovimientoSaldoRepository movimientos;

    @Autowired
    private NotificacionRepository notificaciones;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CuotaHistoricaRepository historico;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento partido;

    @AfterEach
    void limpiar() {
        for (String email : new String[] {ANA, LUIS}) {
            usuarios.findByEmail(email).ifPresent(u -> {
                retoRepository.deleteAll(retoRepository.deUsuario(u));
                notificaciones.deleteAll(notificaciones.findAll().stream()
                        .filter(n -> n.getUsuario().getId().equals(u.getId())).toList());
            });
        }
        ligaRepository.findAll().stream().filter(l -> l.getCreador().getEmail().equals(ANA)).forEach(ligaRepository::delete);
        if (partido != null) {
            historico.deleteAll(historico.findByEventoOrderByFechaAscIdAsc(partido));
            eventos.deleteById(partido.getId());
        }
        for (String email : new String[] {ANA, LUIS}) {
            usuarios.findByEmail(email).ifPresent(u -> movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u)));
            usuarios.findByEmail(email).ifPresent(usuarios::delete);
        }
    }

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    @Test
    void aceptarloCuandoElPartidoYaNoAdmiteApuestasLoAnulaYDevuelveElImporte() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        Usuario luis = usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        ligas.unirse(LUIS, ligas.crear(ANA, "Reto caducado").getCodigo());
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
        BigDecimal antes = saldo(ANA);
        Reto reto = retos.retar(ANA, luis.getId(), partido.getId(), Resultado.LOCAL, BigDecimal.TEN);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes.subtract(BigDecimal.TEN));

        // El partido deja de admitir apuestas antes de que Luis conteste
        Evento suspendido = eventos.findById(partido.getId()).orElseThrow();
        suspendido.suspender();
        eventos.save(suspendido);

        BigDecimal luisAntes = saldo(LUIS);
        assertThatThrownBy(() -> retos.aceptar(LUIS, reto.getId(), Resultado.VISITANTE))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("se ha devuelto el importe");
        // Lo que dice el mensaje se ha guardado: el reto está anulado y Ana tiene su dinero; a Luis no se le cobra
        assertThat(retoRepository.findById(reto.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReto.DEVUELTO);
        assertThat(saldo(ANA)).isEqualByComparingTo(antes);
        assertThat(saldo(LUIS)).isEqualByComparingTo(luisAntes);
    }
}
