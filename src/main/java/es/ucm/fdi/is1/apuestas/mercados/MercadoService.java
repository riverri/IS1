package es.ucm.fdi.is1.apuestas.mercados;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;

/** Mercados a largo plazo: consulta pública (HU-44) y alta y edición por el creador (HU-45). */
@Service
public class MercadoService {

    private final MercadoRepository mercados;
    private final CandidatoRepository candidatos;
    private final EquipoRepository equipos;
    private final Clock reloj;

    public MercadoService(MercadoRepository mercados, CandidatoRepository candidatos, EquipoRepository equipos,
                          Clock reloj) {
        this.mercados = mercados;
        this.candidatos = candidatos;
        this.equipos = equipos;
        this.reloj = reloj;
    }

    /** Mercados que admiten apuestas ahora mismo, del que cierra antes al que cierra después. */
    @Transactional(readOnly = true)
    public List<Mercado> abiertos() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return mercados.findAllByOrderByCierreAsc().stream().filter(m -> m.admiteApuestas(ahora)).toList();
    }

    /** Mercados cerrados o ya resueltos, pendientes de ganador o con él (no los anulados). */
    @Transactional(readOnly = true)
    public List<Mercado> terminados() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return mercados.findAllByOrderByCierreAsc().stream()
                .filter(m -> !m.admiteApuestas(ahora) && m.getEstado() != EstadoMercado.ANULADO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Mercado> todos() {
        return mercados.findAllByOrderByCierreAsc();
    }

    @Transactional(readOnly = true)
    public Mercado mercado(Long id) {
        return mercados.findById(id).orElseThrow(() -> new MercadoNoDisponibleException(id));
    }

    /**
     * Crea un mercado con sus candidatos, uno por línea con el formato "Nombre; cuota" (HU-45).
     * Si un candidato se llama igual que un equipo o deportista del mismo deporte, se enlaza para mostrar su escudo.
     */
    @Transactional
    public Mercado crear(String nombre, Deporte deporte, LocalDateTime cierre, String lineasCandidatos) {
        if (!cierre.isAfter(LocalDateTime.now(reloj))) {
            throw new IllegalArgumentException("La fecha de cierre tiene que ser futura");
        }
        List<String[]> leidos = leerCandidatos(lineasCandidatos);
        if (leidos.size() < 2) {
            throw new IllegalArgumentException("Hacen falta al menos 2 candidatos");
        }
        Mercado mercado = new Mercado(nombre.trim(), deporte, cierre);
        for (String[] candidato : leidos) {
            anadir(mercado, candidato[0], cuota(candidato[1], candidato[0]));
        }
        return mercados.save(mercado);
    }

    @Transactional
    public Candidato anadirCandidato(Long mercadoId, String nombre, BigDecimal cuota) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre del candidato");
        }
        return anadir(mercado(mercadoId), nombre.trim(), cuota);
    }

    /** Las apuestas ya hechas conservan su cuota; solo cambia para las nuevas. */
    @Transactional
    public Candidato cambiarCuota(Long mercadoId, Long candidatoId, BigDecimal cuota) {
        Candidato candidato = candidatos.findById(candidatoId)
                .filter(c -> c.getMercado().getId().equals(mercadoId))
                .orElseThrow(() -> new MercadoNoDisponibleException(mercadoId));
        EstadoMercado estado = candidato.getMercado().getEstado();
        if (estado != EstadoMercado.ABIERTO && estado != EstadoMercado.CERRADO) {
            throw new IllegalStateException("El mercado ya está resuelto o anulado");
        }
        candidato.cambiarCuota(cuota);
        return candidato;
    }

    @Transactional
    public void cerrar(Long mercadoId) {
        mercado(mercadoId).cerrar();
    }

    private Candidato anadir(Mercado mercado, String nombre, BigDecimal cuota) {
        Candidato candidato = mercado.anadirCandidato(nombre, cuota);
        equipos.findByNombre(nombre)
                .filter(e -> e.getDeporte() == mercado.getDeporte())
                .ifPresent(candidato::setEquipo);
        return candidato;
    }

    private static List<String[]> leerCandidatos(String texto) {
        List<String[]> leidos = new ArrayList<>();
        if (texto == null) {
            return leidos;
        }
        for (String linea : texto.split("\\R")) {
            if (linea.isBlank()) {
                continue;
            }
            String[] partes = linea.split(";");
            if (partes.length != 2 || partes[0].isBlank()) {
                throw new IllegalArgumentException("Línea no válida: \"" + linea.trim()
                        + "\". El formato es: Nombre; cuota");
            }
            leidos.add(new String[] {partes[0].trim(), partes[1].trim()});
        }
        return leidos;
    }

    private static BigDecimal cuota(String texto, String candidato) {
        try {
            return new BigDecimal(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La cuota de " + candidato + " no es un número: " + texto);
        }
    }
}
