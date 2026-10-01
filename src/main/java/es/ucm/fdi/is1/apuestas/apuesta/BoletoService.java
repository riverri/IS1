package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
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
        Evento evento = disponible(eventoId);
        BigDecimal cuota = calculadora.calcular(evento).de(resultado);
        if (cuota == null) {
            throw new ResultadoNoValidoException(resultado);
        }
        int maximo = limites.actuales().getMaxSelecciones();
        if (boleto.getTamano() >= maximo) {
            throw new IllegalArgumentException("El boleto admite como máximo " + maximo + " selecciones");
        }
        boleto.anadir(eventoId, resultado, cuota);
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
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<BoletoVista.LineaVista> lineas = new ArrayList<>();
        for (Boleto.Linea linea : List.copyOf(boleto.getLineas())) {
            Evento evento = eventos.findById(linea.eventoId()).filter(e -> e.admiteApuestas(ahora)).orElse(null);
            if (evento == null) {
                boleto.quitar(linea.eventoId());
                continue;
            }
            BigDecimal cuota = calculadora.calcular(evento).de(linea.resultado());
            lineas.add(new BoletoVista.LineaVista(evento, linea.resultado(), cuota,
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
                .map(l -> new SeleccionPedida(l.eventoId(), l.resultado(), l.cuotaVista()))
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
