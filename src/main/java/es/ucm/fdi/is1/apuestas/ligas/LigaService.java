package es.ucm.fdi.is1.apuestas.ligas;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.CriterioRanking;
import es.ucm.fdi.is1.apuestas.apuesta.PuestoRanking;
import es.ucm.fdi.is1.apuestas.apuesta.RankingService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/** Ligas privadas entre amigos con código de invitación y ranking propio (HU-51). */
@Service
public class LigaService {

    /** Sin letras ni números que se confundan al dictarlos (0/O, 1/I/L). */
    static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    static final int LONGITUD_CODIGO = 6;
    static final int MAX_LIGAS = 10;

    private final LigaRepository ligas;
    private final UsuarioRepository usuarios;
    private final RankingService rankings;
    private final Clock reloj;
    private final SecureRandom azar = new SecureRandom();

    public LigaService(LigaRepository ligas, UsuarioRepository usuarios, RankingService rankings, Clock reloj) {
        this.ligas = ligas;
        this.usuarios = usuarios;
        this.rankings = rankings;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<Liga> mias(String email) {
        return ligas.deMiembro(usuario(email));
    }

    /** Crea la liga con un código nuevo; quien la crea es su primer miembro. */
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

    /** Entra en la liga del código (sin distinguir mayúsculas ni espacios). */
    @Transactional
    public Liga unirse(String email, String codigo) {
        Usuario usuario = jugador(email);
        String limpio = codigo == null ? "" : codigo.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
        Liga liga = ligas.findByCodigo(limpio)
                .orElseThrow(() -> new IllegalArgumentException("No hay ninguna liga con el código " + limpio));
        if (liga.esMiembro(usuario)) {
            throw new IllegalArgumentException("Ya estás en la liga " + liga.getNombre());
        }
        if (ligas.deMiembro(usuario).size() >= MAX_LIGAS) {
            throw new IllegalArgumentException("Puedes estar como mucho en " + MAX_LIGAS + " ligas");
        }
        liga.unir(usuario);
        return liga;
    }

    /** Solo la ven sus miembros. */
    @Transactional(readOnly = true)
    public Liga ver(Long id, String email) {
        Usuario usuario = usuario(email);
        return ligas.findById(id).filter(l -> l.esMiembro(usuario))
                .orElseThrow(() -> new LigaNoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public List<PuestoRanking> ranking(Liga liga, String email, CriterioRanking criterio) {
        return rankings.ranking(email, criterio, liga.getIdsMiembros());
    }

    /** Sale de la liga. Quien la creó no puede salir: la borra. */
    @Transactional
    public Liga salir(Long id, String email) {
        Liga liga = ver(id, email);
        Usuario usuario = usuario(email);
        if (liga.esCreador(usuario)) {
            throw new IllegalArgumentException("Has creado tú la liga: si no quieres seguir, bórrala");
        }
        liga.salir(usuario);
        return liga;
    }

    /** Solo la puede borrar quien la creó. */
    @Transactional
    public Liga borrar(Long id, String email) {
        Liga liga = ver(id, email);
        if (!liga.esCreador(usuario(email))) {
            throw new IllegalArgumentException("Solo quien ha creado la liga la puede borrar");
        }
        ligas.delete(liga);
        return liga;
    }

    private String codigoNuevo() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < LONGITUD_CODIGO; i++) {
                sb.append(ALFABETO.charAt(azar.nextInt(ALFABETO.length())));
            }
            codigo = sb.toString();
        } while (ligas.existsByCodigo(codigo));
        return codigo;
    }

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
