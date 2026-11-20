package es.ucm.fdi.is1.apuestas.ligas;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.CriterioRanking;
import es.ucm.fdi.is1.apuestas.apuesta.PuestoRanking;
import es.ucm.fdi.is1.apuestas.apuesta.RankingService;
import es.ucm.fdi.is1.apuestas.usuarios.CuentaEliminada;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Servicio de las ligas privadas entre amigos, con código de invitación y ranking propio (HU-51).
 * El ranking de una liga es el general de {@link RankingService} filtrado a sus miembros, así que usa
 * los mismos criterios (CA-51.2). Los usuarios llegan por su email, el de la sesión iniciada.
 */
@Service
public class LigaService {

    /** Sin letras ni números que se confundan al dictarlos (0/O, 1/I/L). */
    static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    static final int LONGITUD_CODIGO = 6;
    /** Ligas en las que puede estar un jugador a la vez, contando las que ha creado. */
    static final int MAX_LIGAS = 10;

    private final LigaRepository ligas;
    private final UsuarioRepository usuarios;
    private final RankingService rankings;
    private final Clock reloj;
    private final ApplicationEventPublisher publicador;
    /** SecureRandom y no Random: los códigos no se pueden adivinar a partir de otros. */
    private final SecureRandom azar = new SecureRandom();

    public LigaService(LigaRepository ligas, UsuarioRepository usuarios, RankingService rankings, Clock reloj,
                       ApplicationEventPublisher publicador) {
        this.ligas = ligas;
        this.usuarios = usuarios;
        this.rankings = rankings;
        this.reloj = reloj;
        this.publicador = publicador;
    }

    /** Ligas en las que está el usuario, por nombre. */
    @Transactional(readOnly = true)
    public List<Liga> mias(String email) {
        return ligas.deMiembro(usuario(email));
    }

    /** Clasificación de las ligas del usuario por la suma del saldo de sus miembros activos. */
    @Transactional(readOnly = true)
    public List<PuestoClasificacionLiga> clasificacion(String email) {
        record TotalLiga(Liga liga, int miembros, BigDecimal saldo) {
        }

        List<TotalLiga> totales = ligas.deMiembro(usuario(email)).stream()
                .map(liga -> {
                    List<Usuario> miembros = liga.getMiembrosActivos();
                    BigDecimal saldo = miembros.stream().map(Usuario::getSaldo)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new TotalLiga(liga, miembros.size(), saldo);
                })
                .sorted(Comparator.comparing(TotalLiga::saldo).reversed()
                        .thenComparing(t -> t.liga().getNombre(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(t -> t.liga().getId()))
                .toList();

        return java.util.stream.IntStream.range(0, totales.size())
                .mapToObj(i -> {
                    TotalLiga total = totales.get(i);
                    return new PuestoClasificacionLiga(i + 1, total.liga().getId(), total.liga().getNombre(),
                            total.miembros(), total.saldo());
                })
                .toList();
    }

    /**
     * Crea la liga con un código nuevo; quien la crea es su primer miembro (CA-51.1).
     *
     * @throws IllegalArgumentException si es el creador de apuestas, el nombre no tiene entre 1 y 40 caracteres
     *                                  o el usuario ya está en {@link #MAX_LIGAS} ligas
     */
    @Transactional
    public Liga crear(String email, String nombre) {
        Usuario usuario = jugador(email);
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty() || limpio.length() > 40) {
            throw new IllegalArgumentException("El nombre de la liga tiene que tener entre 1 y 40 caracteres");
        }
        if (ligas.deMiembro(usuario).size() >= MAX_LIGAS) {
            throw new IllegalArgumentException("Puedes estar como mucho en " + MAX_LIGAS + " ligas");
        }
        return ligas.save(new Liga(limpio, codigoNuevo(), usuario, LocalDateTime.now(reloj)));
    }

    /**
     * Entra en la liga del código (sin distinguir mayúsculas ni espacios) (CA-51.2).
     *
     * @throws IllegalArgumentException si es el creador de apuestas, el código no existe, ya es miembro
     *                                  o ya está en {@link #MAX_LIGAS} ligas
     */
    @Transactional
    public Liga unirse(String email, String codigo) {
        Usuario usuario = jugador(email);
        // "abc 123" y "ABC123" son el mismo código: se quitan los espacios y se pasa a mayúsculas
        // ("\\s" en una expresión regular = cualquier espacio en blanco)
        String limpio = codigo == null ? "" : codigo.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
        Liga liga = ligas.findByCodigo(limpio)
                .orElseThrow(() -> new IllegalArgumentException("No hay ninguna liga con el código " + limpio));
        if (liga.esMiembro(usuario)) {
            throw new IllegalArgumentException("Ya estás en la liga " + liga.getNombre());
        }
        if (ligas.deMiembro(usuario).size() >= MAX_LIGAS) {
            throw new IllegalArgumentException("Puedes estar como mucho en " + MAX_LIGAS + " ligas");
        }
        // No hace falta save: la liga está gestionada por JPA y el cambio se guarda al acabar la transacción
        liga.unir(usuario);
        return liga;
    }

    /**
     * Devuelve la liga si el usuario es miembro. Solo la ven sus miembros.
     *
     * @throws LigaNoEncontradaException si no existe o el usuario no es miembro
     */
    @Transactional(readOnly = true)
    public Liga ver(Long id, String email) {
        Usuario usuario = usuario(email);
        return ligas.findById(id).filter(l -> l.esMiembro(usuario))
                .orElseThrow(() -> new LigaNoEncontradaException(id));
    }

    /**
     * Ranking de la liga con el criterio elegido (saldo, ganancias o aciertos), solo con sus miembros.
     * El email sirve para destacar el puesto de quien lo consulta.
     */
    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(Liga liga, String email, CriterioRanking criterio) {
        return rankings.ranking(email, criterio, liga.getIdsMiembros());
    }

    /**
     * Sale de la liga y deja de aparecer en su ranking (CA-51.3). Quien la creó no puede salir: la borra.
     *
     * @throws LigaNoEncontradaException si no existe o no es miembro
     * @throws IllegalArgumentException  si es quien la creó
     */
    @Transactional
    public Liga salir(Long id, String email) {
        Liga liga = ver(id, email);
        Usuario usuario = usuario(email);
        if (liga.esCreador(usuario)) {
            throw new IllegalArgumentException("Has creado tú la liga: si no quieres seguir, bórrala");
        }
        liga.salir(usuario);
        // Sus retos pendientes con quien ya no comparta ninguna liga se devuelven (RetoService)
        publicador.publishEvent(new MiembrosLigaCambiados(List.of(usuario.getId())));
        return liga;
    }

    /**
     * Borra la liga con todos sus miembros (CA-51.3). Solo la puede borrar quien la creó.
     *
     * @throws LigaNoEncontradaException si no existe o no es miembro
     * @throws IllegalArgumentException  si no es quien la creó
     */
    @Transactional
    public Liga borrar(Long id, String email) {
        Liga liga = ver(id, email);
        if (!liga.esCreador(usuario(email))) {
            throw new IllegalArgumentException("Solo quien ha creado la liga la puede borrar");
        }
        List<Long> miembros = List.copyOf(liga.getIdsMiembros());
        ligas.delete(liga);
        publicador.publishEvent(new MiembrosLigaCambiados(miembros));
        return liga;
    }

    /**
     * Al eliminar una cuenta: se borran las ligas que creó (nadie más podría borrarlas) y sale de las demás.
     * Escucha el evento {@link CuentaEliminada} que publica la baja (HU-18); se ejecuta en el momento,
     * dentro de la misma transacción.
     */
    @EventListener
    public void alEliminarCuenta(CuentaEliminada cuenta) {
        Usuario usuario = usuarios.findById(cuenta.usuarioId()).orElseThrow();
        // Quien se queda sin compartir liga con alguien: el propio usuario y los miembros de las ligas que se borran
        Set<Long> afectados = new LinkedHashSet<>(List.of(usuario.getId()));
        for (Liga liga : ligas.deMiembro(usuario)) {
            if (liga.esCreador(usuario)) {
                afectados.addAll(liga.getIdsMiembros());
                ligas.delete(liga);
            } else {
                liga.salir(usuario);
            }
        }
        // Como al salir o borrar una liga: los retos pendientes se devuelven (RetoService). Se publica antes de que la
        // baja ponga el saldo a cero, así que lo del propio usuario se pierde con su saldo, como sus apuestas
        publicador.publishEvent(new MiembrosLigaCambiados(List.copyOf(afectados)));
    }

    /** Genera códigos al azar con {@link #ALFABETO} hasta dar con uno que no use otra liga. */
    private String codigoNuevo() {
        String codigo;
        // do-while: el cuerpo se ejecuta al menos una vez y se repite mientras el código ya exista
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < LONGITUD_CODIGO; i++) {
                // nextInt(n) da un número al azar entre 0 y n-1: la posición de una letra del alfabeto
                sb.append(ALFABETO.charAt(azar.nextInt(ALFABETO.length())));
            }
            codigo = sb.toString();
        } while (ligas.existsByCodigo(codigo));
        return codigo;
    }

    /** Usuario de la sesión; siempre existe porque ha iniciado sesión. */
    private Usuario usuario(String email) {
        return usuarios.findByEmail(email).orElseThrow();
    }

    /** El creador de apuestas no juega, así que no entra en ligas. */
    private Usuario jugador(String email) {
        Usuario usuario = usuario(email);
        if (usuario.getRol() != Rol.USUARIO) {
            throw new IllegalArgumentException("El creador de apuestas no juega, así que no puede entrar en ligas");
        }
        return usuario;
    }
}
