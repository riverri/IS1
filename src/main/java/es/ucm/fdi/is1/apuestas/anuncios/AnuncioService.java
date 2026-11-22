package es.ucm.fdi.is1.apuestas.anuncios;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final ObjectProvider<WebInvocationPrivilegeEvaluator> permisos;
    private final Map<Long, AtomicLong> impresionesPendientes = new ConcurrentHashMap<>();
    private final TransactionTemplate transaccion;
    private volatile CacheAnuncios anunciosActivosCache;

    private record CacheAnuncios(List<Anuncio> anuncios, long guardadoEn) {
    }

    private static final long DURACION_CACHE_NS = java.time.Duration.ofSeconds(5).toNanos();

    /** Un visitante sin sesión, para preguntar a Spring Security si podría abrir una página. */
    private static final AnonymousAuthenticationToken VISITANTE = new AnonymousAuthenticationToken("anuncios",
            "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    public AnuncioService(AnuncioRepository anuncios, ClicAnuncioRepository clics, UsuarioRepository usuarios,
                          RandomGenerator azar, Clock reloj, ObjectProvider<WebInvocationPrivilegeEvaluator> permisos,
                          PlatformTransactionManager gestorTransacciones) {
        this.anuncios = anuncios;
        this.clics = clics;
        this.usuarios = usuarios;
        this.azar = azar;
        this.reloj = reloj;
        this.permisos = permisos;
        this.transaccion = new TransactionTemplate(gestorTransacciones);
    }

    /**
     * Anuncio para la página {@code ruta}, o vacío si no es de ninguna sección o no hay ninguno que mostrar.
     * Si hay uno, cuenta una impresión (HU-62).
     */
    @Transactional
    public Optional<Anuncio> mostrar(String ruta, boolean conSesion) {
        Optional<Anuncio> elegido = elegirPara(ruta, conSesion);
        elegido.ifPresent(a -> registrarImpresion(a.getId()));
        return elegido;
    }

    /**
     * El anuncio para esa página, sin contar la impresión: la cuenta {@link ImpresionesAnuncio} cuando la página se
     * llega a pintar (no en una descarga, una redirección o un error).
     *
     * <p>A un visitante sin sesión solo se le enseñan anuncios cuyo destino puede abrir, según las mismas reglas de
     * {@code SeguridadConfig}: /eventos sí, pero /eventos/7/apostar o /cripto/cartera no (CA-60.3).
     */
    @Transactional(readOnly = true)
    public Optional<Anuncio> elegirPara(String ruta, boolean conSesion) {
        Optional<Ubicacion> donde = Ubicacion.de(ruta);
        if (donde.isEmpty()) {
            return Optional.empty();
        }
        List<Anuncio> candidatos = anunciosActivos();
        WebInvocationPrivilegeEvaluator evaluador = permisos.getIfAvailable();
        if (!conSesion && evaluador != null) {
            candidatos = candidatos.stream()
                    .filter(a -> evaluador.isAllowed("", soloRuta(a.getDestino()), "GET", VISITANTE)).toList();
            // Ya se ha comprobado con las reglas de verdad: la de la sección (Ubicacion.requiereSesion) sobra
            return elegir(candidatos, donde.get(), true, LocalDateTime.now(reloj), azar);
        }
        return elegir(candidatos, donde.get(), conSesion, LocalDateTime.now(reloj), azar);
    }

    /** La lista activa es pequeña y se comparte entre peticiones; vence pronto para limitar obsolescencia entre nodos. */
    private List<Anuncio> anunciosActivos() {
        long ahora = System.nanoTime();
        CacheAnuncios cache = anunciosActivosCache;
        if (cache != null && ahora - cache.guardadoEn() < DURACION_CACHE_NS) {
            return cache.anuncios();
        }
        synchronized (this) {
            ahora = System.nanoTime();
            cache = anunciosActivosCache;
            if (cache == null || ahora - cache.guardadoEn() >= DURACION_CACHE_NS) {
                cache = new CacheAnuncios(List.copyOf(anuncios.findByActivoTrue()), ahora);
                anunciosActivosCache = cache;
            }
            return cache.anuncios();
        }
    }

    /** Evita publicar valores sin confirmar; el vencimiento de cinco segundos cubre cambios desde otra instancia. */
    private void invalidarAnunciosActivosAlConfirmar() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            anunciosActivosCache = null;
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invalidarCacheAnuncios();
            }
        });
    }

    /** Invalida la copia tras cambios externos; el vencimiento sigue limitando su obsolescencia entre instancias. */
    void invalidarCacheAnuncios() {
        anunciosActivosCache = null;
    }

    /** El destino sin {@code ?...} ni {@code #...}: las reglas de seguridad se comparan solo con la ruta. */
    static String soloRuta(String destino) {
        return destino.split("[?#]", 2)[0];
    }

    /** Registra la impresión sin escribir en la base de datos durante la petición que renderiza la página. */
    public void registrarImpresion(Long id) {
        impresionesPendientes.computeIfAbsent(id, clave -> new AtomicLong()).incrementAndGet();
    }

    /** Persiste juntas las impresiones acumuladas; los contadores se restauran si falla el lote. */
    @Scheduled(initialDelayString = "PT5S", fixedDelayString = "PT5S")
    public void vaciarImpresiones() {
        Map<Long, Long> lote = new HashMap<>();
        impresionesPendientes.forEach((id, contador) -> {
            long cantidad = contador.getAndSet(0);
            if (cantidad > 0) {
                lote.put(id, cantidad);
            }
        });
        try {
            transaccion.executeWithoutResult(estado -> lote.forEach(anuncios::sumarImpresiones));
        } catch (RuntimeException e) {
            lote.forEach((id, cantidad) -> impresionesPendientes.get(id).addAndGet(cantidad));
            throw e;
        }
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
        // Solo cuenta si se podía ver: activo y dentro de sus fechas (un enlace viejo a uno caducado no suma)
        Optional<Anuncio> anuncio = anuncios.findById(id).filter(a -> a.vigente(LocalDateTime.now(reloj)));
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
        Anuncio creado = anuncios.save(new Anuncio(titulo, texto, icono, destino, ubicaciones, prioridad, desde, hasta));
        invalidarAnunciosActivosAlConfirmar();
        return creado;
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
        invalidarAnunciosActivosAlConfirmar();
        return anuncio;
    }

    /** Activa o desactiva un anuncio sin borrarlo, para no perder sus estadísticas. */
    @Transactional
    public Anuncio activar(Long id, boolean activo) {
        Anuncio anuncio = anuncio(id);
        anuncio.activar(activo);
        invalidarAnunciosActivosAlConfirmar();
        return anuncio;
    }
}
