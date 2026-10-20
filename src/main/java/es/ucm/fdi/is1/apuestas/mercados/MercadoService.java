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

/**
 * Servicio de los mercados a largo plazo: consulta pública (HU-44) y alta y edición por el creador (HU-45).
 * Lo usan {@code MercadosController} (catálogo) y {@code GestionMercadosController} (panel del creador).
 * Marcar el ganador y anular, que mueven dinero, están en {@code ResolucionMercadosService}.
 */
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

    /**
     * Mercados cerrados o ya resueltos, pendientes de ganador o con él (no los anulados).
     * Incluye los que siguen ABIERTO pero ya han pasado su fecha de cierre.
     */
    @Transactional(readOnly = true)
    public List<Mercado> terminados() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return mercados.findAllByOrderByCierreAsc().stream()
                .filter(m -> !m.admiteApuestas(ahora) && m.getEstado() != EstadoMercado.ANULADO)
                .toList();
    }

    /** Todos los mercados, en cualquier estado, para el panel del creador. */
    @Transactional(readOnly = true)
    public List<Mercado> todos() {
        return mercados.findAllByOrderByCierreAsc();
    }

    /** @throws MercadoNoDisponibleException si no existe un mercado con ese id */
    @Transactional(readOnly = true)
    public Mercado mercado(Long id) {
        return mercados.findById(id).orElseThrow(() -> new MercadoNoDisponibleException(id));
    }

    /**
     * Crea un mercado con sus candidatos, uno por línea con el formato "Nombre; cuota" (HU-45).
     * Si un candidato se llama igual que un equipo o deportista del mismo deporte, se enlaza para mostrar su escudo.
     * Se valida todo antes de guardar: si una línea está mal, no se crea nada.
     *
     * @throws IllegalArgumentException si el cierre no es futuro, hay menos de 2 candidatos, una línea no
     *                                  tiene el formato, una cuota o un nombre no son válidos o un nombre se repite
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
        // Si algún candidato falla, la excepción llega antes del save y el mercado no se guarda
        Mercado mercado = new Mercado(nombre.trim(), deporte, cierre);
        for (String[] candidato : leidos) {
            anadir(mercado, candidato[0], cuota(candidato[1], candidato[0]));
        }
        return mercados.save(mercado);
    }

    /**
     * Añade un candidato a un mercado ya creado. Solo si el mercado sigue abierto.
     *
     * @throws IllegalArgumentException si el nombre está vacío o repetido, o la cuota no es válida
     * @throws IllegalStateException    si el mercado no está abierto
     */
    @Transactional
    public Candidato anadirCandidato(Long mercadoId, String nombre, BigDecimal cuota) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre del candidato");
        }
        return anadir(mercado(mercadoId), nombre.trim(), cuota);
    }

    /**
     * Cambia la cuota de un candidato. Las apuestas ya hechas conservan su cuota; solo cambia para las nuevas.
     * Se puede con el mercado abierto o cerrado, pero no si ya está resuelto o anulado.
     *
     * @throws MercadoNoDisponibleException si el candidato no existe o no es de ese mercado
     * @throws IllegalStateException        si el mercado está resuelto o anulado
     * @throws IllegalArgumentException     si la cuota no es válida
     */
    @Transactional
    public Candidato cambiarCuota(Long mercadoId, Long candidatoId, BigDecimal cuota) {
        Candidato candidato = candidatos.findById(candidatoId)
                // Se comprueba que el candidato es de ese mercado: el id del mercado viene en la URL
                .filter(c -> c.getMercado().getId().equals(mercadoId))
                .orElseThrow(() -> new MercadoNoDisponibleException(mercadoId));
        EstadoMercado estado = candidato.getMercado().getEstado();
        if (estado != EstadoMercado.ABIERTO && estado != EstadoMercado.CERRADO) {
            throw new IllegalStateException("El mercado ya está resuelto o anulado");
        }
        candidato.cambiarCuota(cuota);
        return candidato;
    }

    /**
     * Cierra el mercado antes de su fecha: deja de admitir apuestas (CA-45.2).
     *
     * @throws IllegalStateException si no está abierto
     */
    @Transactional
    public void cerrar(Long mercadoId) {
        mercado(mercadoId).cerrar();
    }

    /** Añade el candidato y, si hay un equipo del mismo deporte con ese nombre exacto, lo enlaza. */
    private Candidato anadir(Mercado mercado, String nombre, BigDecimal cuota) {
        Candidato candidato = mercado.anadirCandidato(nombre, cuota);
        equipos.findByNombre(nombre)
                .filter(e -> e.getDeporte() == mercado.getDeporte())
                .ifPresent(candidato::setEquipo);
        return candidato;
    }

    /**
     * Lee el texto del formulario: una línea por candidato, "Nombre; cuota". Las líneas vacías se saltan.
     * Devuelve pares [nombre, cuota como texto].
     */
    private static List<String[]> leerCandidatos(String texto) {
        List<String[]> leidos = new ArrayList<>();
        if (texto == null) {
            return leidos;
        }
        // \R es cualquier salto de línea (\n en Linux, \r\n en Windows)
        for (String linea : texto.split("\\R")) {
            if (linea.isBlank()) {
                continue;
            }
            // Exactamente un ";" y un nombre no vacío; la cuota se comprueba después
            String[] partes = linea.split(";");
            if (partes.length != 2 || partes[0].isBlank()) {
                throw new IllegalArgumentException("Línea no válida: \"" + linea.trim()
                        + "\". El formato es: Nombre; cuota");
            }
            leidos.add(new String[] {partes[0].trim(), partes[1].trim()});
        }
        return leidos;
    }

    /** Convierte la cuota; admite coma decimal ("2,50") además de punto. El rango lo comprueba Candidato. */
    private static BigDecimal cuota(String texto, String candidato) {
        try {
            return new BigDecimal(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La cuota de " + candidato + " no es un número: " + texto);
        }
    }
}
