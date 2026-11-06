package es.ucm.fdi.is1.apuestas.anuncios;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.web.RutaSegura;

/**
 * Anuncios internos (HU-60, HU-61, HU-62): cuál se muestra en cada página, el registro de impresiones y clics,
 * y el alta y edición desde Gestión.
 *
 * <p>Elección (CA-60.1): de los anuncios que {@link Anuncio#seMuestra se pueden mostrar} en la sección actual, se
 * queda con los de prioridad más alta y elige uno al azar entre ellos, así los de la misma prioridad se van turnando.
 */
@Service
public class AnuncioService {

    private final AnuncioRepository anuncios;
    private final ClicAnuncioRepository clics;
    private final UsuarioRepository usuarios;
    private final RandomGenerator azar;
    private final Clock reloj;

    public AnuncioService(AnuncioRepository anuncios, ClicAnuncioRepository clics, UsuarioRepository usuarios,
                          RandomGenerator azar, Clock reloj) {
        this.anuncios = anuncios;
        this.clics = clics;
        this.usuarios = usuarios;
        this.azar = azar;
        this.reloj = reloj;
    }

    /**
     * Anuncio para la página {@code ruta}, o vacío si no es de ninguna sección o no hay ninguno que mostrar.
     * Si hay uno, cuenta una impresión (HU-62).
     */
    @Transactional
    public Optional<Anuncio> mostrar(String ruta, boolean conSesion) {
        Optional<Ubicacion> donde = Ubicacion.de(ruta);
        if (donde.isEmpty()) {
            return Optional.empty();
        }
        Optional<Anuncio> elegido = elegir(anuncios.findByActivoTrue(), donde.get(), conSesion,
                LocalDateTime.now(reloj), azar);
        elegido.ifPresent(a -> anuncios.sumarImpresion(a.getId()));
        return elegido;
    }

    /** CA-60.1: los de prioridad más alta que se pueden mostrar, y uno al azar entre ellos. */
    static Optional<Anuncio> elegir(List<Anuncio> candidatos, Ubicacion donde, boolean conSesion, LocalDateTime ahora,
                                    RandomGenerator azar) {
        List<Anuncio> posibles = candidatos.stream().filter(a -> a.seMuestra(donde, conSesion, ahora)).toList();
        if (posibles.isEmpty()) {
            return Optional.empty();
        }
        int maxima = posibles.stream().mapToInt(Anuncio::getPrioridad).max().orElseThrow();
        // Ordenados por id para que, con la misma semilla, el sorteo dé siempre el mismo resultado
        List<Anuncio> mejores = posibles.stream().filter(a -> a.getPrioridad() == maxima)
                .sorted(Comparator.comparing(Anuncio::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        return Optional.of(mejores.get(azar.nextInt(mejores.size())));
    }

    /**
     * Apunta el clic (HU-62) y devuelve a dónde hay que ir. Un anuncio que ya no existe o está desactivado no cuenta
     * y lleva a la portada.
     *
     * @param origen página en la que estaba el anuncio; solo se guarda si es de esta web
     * @param email  quién lo pulsa; null si es un visitante
     */
    @Transactional
    public String registrarClic(Long id, String origen, String email) {
        Optional<Anuncio> anuncio = anuncios.findById(id).filter(Anuncio::isActivo);
        if (anuncio.isEmpty()) {
            return "/";
        }
        clics.save(new ClicAnuncio(anuncio.get(), email == null ? null : usuarios.findByEmail(email).orElse(null),
                RutaSegura.de(origen, "/"), LocalDateTime.now(reloj)));
        return anuncio.get().getDestino();
    }

    /** Todos los anuncios con sus impresiones y clics, para Gestión (HU-62). */
    @Transactional(readOnly = true)
    public List<EstadisticaAnuncio> estadisticas() {
        Map<Long, Long> porAnuncio = new HashMap<>();
        for (Object[] fila : clics.clicsPorAnuncio()) {
            porAnuncio.put((Long) fila[0], ((Number) fila[1]).longValue());
        }
        return anuncios.findAllByOrderByActivoDescPrioridadDescTituloAsc().stream()
                .map(a -> new EstadisticaAnuncio(a, porAnuncio.getOrDefault(a.getId(), 0L)))
                .toList();
    }

    /** @throws AnuncioNoEncontradoException si no existe (404) */
    @Transactional(readOnly = true)
    public Anuncio anuncio(Long id) {
        return anuncios.findById(id).orElseThrow(() -> new AnuncioNoEncontradoException(id));
    }

    /**
     * Crea un anuncio activo (HU-61).
     *
     * @throws IllegalArgumentException si algún dato no es válido (ver {@link Anuncio#cambiar}); el mensaje se muestra
     */
    @Transactional
    public Anuncio crear(String titulo, String texto, String icono, String destino, Set<Ubicacion> ubicaciones,
                         int prioridad, LocalDateTime desde, LocalDateTime hasta) {
        return anuncios.save(new Anuncio(titulo, texto, icono, destino, ubicaciones, prioridad, desde, hasta));
    }

    /**
     * Cambia los datos de un anuncio (HU-61). Sus impresiones y clics se conservan.
     *
     * @throws IllegalArgumentException si algún dato no es válido
     */
    @Transactional
    public Anuncio editar(Long id, String titulo, String texto, String icono, String destino,
                          Set<Ubicacion> ubicaciones, int prioridad, LocalDateTime desde, LocalDateTime hasta) {
        Anuncio anuncio = anuncio(id);
        anuncio.cambiar(titulo, texto, icono, destino, ubicaciones, prioridad, desde, hasta);
        return anuncio;
    }

    /** Activa o desactiva un anuncio sin borrarlo, para no perder sus estadísticas. */
    @Transactional
    public Anuncio activar(Long id, boolean activo) {
        Anuncio anuncio = anuncio(id);
        anuncio.activar(activo);
        return anuncio;
    }
}
