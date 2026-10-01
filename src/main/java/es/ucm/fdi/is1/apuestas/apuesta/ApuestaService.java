package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

@Service
public class ApuestaService {

    private final ApuestaRepository apuestas;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final CalculadoraCuotas calculadora;
    private final Clock reloj;

    public ApuestaService(ApuestaRepository apuestas, EventoRepository eventos, UsuarioRepository usuarios,
                          CalculadoraCuotas calculadora, Clock reloj) {
        this.apuestas = apuestas;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.calculadora = calculadora;
        this.reloj = reloj;
    }

    /**
     * Registra una apuesta simple (HU-23): descuenta el importe del saldo y guarda la cuota vigente.
     * La cuota se calcula en el servidor; nunca se acepta la que envía el navegador.
     */
    @Transactional
    public Apuesta apostar(String email, Long eventoId, Resultado resultado, BigDecimal importe) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Evento evento = eventos.findById(eventoId)
                .filter(e -> e.admiteApuestas(ahora))
                .orElseThrow(() -> new EventoNoDisponibleException(eventoId));
        BigDecimal cuota = calculadora.calcular(evento).de(resultado);
        if (cuota == null) {
            throw new ResultadoNoValidoException(resultado);
        }
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        usuario.cargar(importe);
        return apuestas.save(new Apuesta(usuario, evento, resultado, importe, cuota, ahora));
    }

    /** Apuestas activas del usuario, de la más reciente a la más antigua (HU-24). */
    @Transactional(readOnly = true)
    public List<Apuesta> activas(String email) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        return apuestas.findByUsuarioAndEstadoOrderByFechaDesc(usuario, EstadoApuesta.ACTIVA);
    }
}
