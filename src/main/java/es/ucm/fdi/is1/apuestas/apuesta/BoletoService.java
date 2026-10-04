package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/** Operaciones sobre el boleto del usuario: añadir, quitar, ver y confirmar (HU-28, HU-29). */
@Service
public class BoletoService {

    private final EventoRepository eventos;
    private final CalculadoraCuotas calculadora;
    private final ApuestaService apuestas;
    private final LimitesService limites;
    private final Clock reloj;

    public BoletoService(EventoRepository eventos, CalculadoraCuotas calculadora, ApuestaService apuestas,
                         LimitesService limites, Clock reloj) {
        this.eventos = eventos;
        this.calculadora = calculadora;
        this.apuestas = apuestas;
        this.limites = limites;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public void anadir(Boleto boleto, Long eventoId, Resultado resultado) {
        anadir(boleto, eventoId, resultado, null);
    }

    /** Añade un resultado (1X2) o un tipo especial (HU-52): exactamente uno de los dos. */
    @Transactional(readOnly = true)
    public void anadir(Boleto boleto, Long eventoId, Resultado resultado, Especial especial) {
        Evento evento = disponible(eventoId);
        BigDecimal cuota = apuestas.cuota(evento, resultado, especial);
        int maximo = limites.actuales().getMaxSelecciones();
        if (boleto.getTamano() >= maximo) {
            throw new IllegalArgumentException("El boleto admite como máximo " + maximo + " selecciones");
        }
        List<BigDecimal> conLaNueva = new ArrayList<>(boleto.getLineas().stream().map(Boleto.Linea::cuotaVista).toList());
        conLaNueva.add(cuota);
        ApuestaService.comprobarCuotaTotal(Apuesta.producto(conLaNueva));
        if (especial != null) {
            boleto.anadir(eventoId, especial, cuota);
        } else {
            boleto.anadir(eventoId, resultado, cuota);
        }
    }

    public void quitar(Boleto boleto, Long eventoId) {
        boleto.quitar(eventoId);
    }

    /**
     * Selecciones con su cuota actual. Quita del boleto los eventos que ya no admiten apuestas
     * (han empezado, se han suspendido…) y marca las que han cambiado de cuota.
     */
    @Transactional(readOnly = true)
    public BoletoVista vista(Boleto boleto) {
        return vista(boleto, true);
    }

    /**
     * Para la barra del boleto que sale en todas las páginas: no quita nada del boleto. Si lo hiciera, al confirmar
     * una combinada con un partido que acaba de empezar se apostaría en silencio una apuesta con menos selecciones.
     */
    @Transactional(readOnly = true)
    public BoletoVista resumen(Boleto boleto) {
        return vista(boleto, false);
    }

    private BoletoVista vista(Boleto boleto, boolean quitarNoDisponibles) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<BoletoVista.LineaVista> lineas = new ArrayList<>();
        for (Boleto.Linea linea : List.copyOf(boleto.getLineas())) {
            Evento evento = eventos.findById(linea.eventoId()).filter(e -> e.admiteApuestas(ahora)).orElse(null);
            if (evento == null) {
                if (quitarNoDisponibles) {
                    boleto.quitar(linea.eventoId());
                }
                continue;
            }
            BigDecimal cuota = calculadora.cuota(evento, linea.resultado(), linea.especial());
            lineas.add(new BoletoVista.LineaVista(evento, linea.resultado(), linea.especial(), cuota,
                    cuota.compareTo(linea.cuotaVista()) != 0));
        }
        BigDecimal total = Apuesta.producto(lineas.stream().map(BoletoVista.LineaVista::cuota).toList());
        return new BoletoVista(lineas, total);
    }

    /**
     * Confirma el boleto como apuesta simple o combinada. Si alguna cuota ha cambiado, actualiza las
     * cuotas vistas y relanza la excepción para que el usuario acepte las nuevas (HU-29).
     */
    @Transactional
    public Apuesta confirmar(String email, Boleto boleto, BigDecimal importe) {
        List<SeleccionPedida> pedidas = boleto.getLineas().stream()
                .map(l -> new SeleccionPedida(l.eventoId(), l.resultado(), l.especial(), l.cuotaVista()))
                .toList();
        try {
            Apuesta apuesta = apuestas.apostar(email, pedidas, importe);
            boleto.vaciar();
            return apuesta;
        } catch (CuotasCambiadasException e) {
            aceptarCuotasActuales(boleto);
            throw e;
        }
    }

    /** Las cuotas que el usuario tenía delante al confirmar, como "evento:cuota". Las mal escritas se ignoran. */
    public void fijarCuotasVistas(Boleto boleto, List<String> cuotas) {
        for (String vista : cuotas) {
            String[] partes = vista.split(":");
            try {
                boleto.actualizarCuota(Long.valueOf(partes[0]), new BigDecimal(partes[1]));
            } catch (RuntimeException e) {
                // valor manipulado o incompleto: se queda la cuota que ya tenía la línea
            }
        }
    }

    private void aceptarCuotasActuales(Boleto boleto) {
        for (BoletoVista.LineaVista linea : vista(boleto).lineas()) {
            boleto.actualizarCuota(linea.evento().getId(), linea.cuota());
        }
    }

    private Evento disponible(Long eventoId) {
        return eventos.findById(eventoId)
                .filter(e -> e.admiteApuestas(LocalDateTime.now(reloj)))
                .orElseThrow(() -> new EventoNoDisponibleException(eventoId));
    }
}
