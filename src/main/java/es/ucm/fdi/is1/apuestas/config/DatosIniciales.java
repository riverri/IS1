package es.ucm.fdi.is1.apuestas.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Datos iniciales: usuarios de prueba, competiciones y equipos.
 * Los eventos (partidos reales) los introduce el creador de apuestas desde el panel de gestión.
 * Solo se cargan si la base de datos está vacía.
 */
@Component
public class DatosIniciales implements ApplicationRunner {

    private final CompeticionRepository competiciones;
    private final EquipoRepository equipos;
    private final UsuarioService usuarios;

    public DatosIniciales(CompeticionRepository competiciones, EquipoRepository equipos, UsuarioService usuarios) {
        this.competiciones = competiciones;
        this.equipos = equipos;
        this.usuarios = usuarios;
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

        // Calificaciones orientativas: el creador de apuestas debe revisarlas
        equipo("Real Madrid", Deporte.FUTBOL, 9.5, laLiga, champions);
        equipo("FC Barcelona", Deporte.FUTBOL, 9.0, laLiga, champions);
        equipo("Atlético de Madrid", Deporte.FUTBOL, 8.5, laLiga, champions);
        equipo("Getafe CF", Deporte.FUTBOL, 5.5, laLiga);
        equipo("Bayern de Múnich", Deporte.FUTBOL, 9.0, champions);
        equipo("Real Madrid Baloncesto", Deporte.BALONCESTO, 9.0, acb, euroliga);
        equipo("Barça Basket", Deporte.BALONCESTO, 8.5, acb, euroliga);
        equipo("Unicaja", Deporte.BALONCESTO, 7.5, acb);
        equipo("Max Verstappen", Deporte.AUTOMOVILISMO, 9.5, f1);
        equipo("Lance Stroll", Deporte.AUTOMOVILISMO, 2.0, f1);
    }

    private void equipo(String nombre, Deporte deporte, double calidad, Competicion... enCompeticiones) {
        Equipo equipo = new Equipo(nombre, deporte, calidad);
        for (Competicion competicion : enCompeticiones) {
            equipo.participaEn(competicion);
        }
        equipos.save(equipo);
    }
}
