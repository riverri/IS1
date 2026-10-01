package es.ucm.fdi.is1.apuestas.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Datos de ejemplo para desarrollo (sin API, como indica el MVP).
 * Solo se cargan si la base de datos está vacía.
 */
@Component
public class DatosIniciales implements ApplicationRunner {

    private final CompeticionRepository competiciones;
    private final EquipoRepository equipos;
    private final EventoRepository eventos;
    private final UsuarioService usuarios;
    private final Clock reloj;

    public DatosIniciales(CompeticionRepository competiciones, EquipoRepository equipos,
                          EventoRepository eventos, UsuarioService usuarios, Clock reloj) {
        this.competiciones = competiciones;
        this.equipos = equipos;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (competiciones.count() > 0) {
            return;
        }

        usuarios.crear("creador@apuestas.es", "Creador de apuestas", "creador123", Rol.CREADOR);
        usuarios.crear("usuario@apuestas.es", "Usuario de prueba", "usuario123", Rol.USUARIO);

        Competicion laLiga = competiciones.save(new Competicion("LaLiga", Deporte.FUTBOL));
        Competicion champions = competiciones.save(new Competicion("Champions League", Deporte.FUTBOL));
        Competicion acb = competiciones.save(new Competicion("Liga ACB", Deporte.BALONCESTO));
        Competicion euroliga = competiciones.save(new Competicion("Euroliga", Deporte.BALONCESTO));
        Competicion f1 = competiciones.save(new Competicion("Fórmula 1", Deporte.AUTOMOVILISMO));

        Equipo realMadrid = equipo("Real Madrid", Deporte.FUTBOL, 9.5, laLiga, champions);
        Equipo barcelona = equipo("FC Barcelona", Deporte.FUTBOL, 9.0, laLiga, champions);
        Equipo atletico = equipo("Atlético de Madrid", Deporte.FUTBOL, 8.5, laLiga, champions);
        Equipo getafe = equipo("Getafe CF", Deporte.FUTBOL, 5.5, laLiga);
        Equipo bayern = equipo("Bayern de Múnich", Deporte.FUTBOL, 9.0, champions);
        Equipo madridBasket = equipo("Real Madrid Baloncesto", Deporte.BALONCESTO, 9.0, acb, euroliga);
        Equipo barcaBasket = equipo("Barça Basket", Deporte.BALONCESTO, 8.5, acb, euroliga);
        Equipo unicaja = equipo("Unicaja", Deporte.BALONCESTO, 7.5, acb);
        equipo("Max Verstappen", Deporte.AUTOMOVILISMO, 9.5, f1);
        equipo("Lance Stroll", Deporte.AUTOMOVILISMO, 2.0, f1);

        LocalDateTime hoy = LocalDateTime.of(LocalDateTime.now(reloj).toLocalDate(), LocalTime.of(21, 0));
        eventos.save(new Evento(laLiga, realMadrid, getafe, hoy.plusDays(1)));
        eventos.save(new Evento(laLiga, atletico, barcelona, hoy.plusDays(2)));
        eventos.save(new Evento(champions, bayern, realMadrid, hoy.plusDays(5)));
        eventos.save(new Evento(acb, unicaja, madridBasket, hoy.plusDays(1).minusHours(2)));
        eventos.save(new Evento(euroliga, barcaBasket, madridBasket, hoy.plusDays(3)));
        // Evento ya jugado: no debe aparecer en el catálogo
        eventos.save(new Evento(laLiga, barcelona, getafe, hoy.minusDays(3)));
    }

    private Equipo equipo(String nombre, Deporte deporte, double calidad, Competicion... enCompeticiones) {
        Equipo equipo = new Equipo(nombre, deporte, calidad);
        for (Competicion competicion : enCompeticiones) {
            equipo.participaEn(competicion);
        }
        return equipos.save(equipo);
    }
}
