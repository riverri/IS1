package es.ucm.fdi.is1.apuestas.config;

import java.time.LocalDateTime;
import java.util.Map;

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
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Datos iniciales: usuarios de prueba, competiciones, equipos y partidos reales de LaLiga y Champions 2026/27.
 * Solo se añade lo que falta (por nombre), así que no duplica ni borra lo que el creador
 * de apuestas haya introducido desde el panel de gestión.
 */
@Component
public class DatosIniciales implements ApplicationRunner {

    private final CompeticionRepository competiciones;
    private final EquipoRepository equipos;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarios;

    public DatosIniciales(CompeticionRepository competiciones, EquipoRepository equipos, EventoRepository eventos,
                          UsuarioRepository usuarioRepository, UsuarioService usuarios) {
        this.competiciones = competiciones;
        this.equipos = equipos;
        this.eventos = eventos;
        this.usuarioRepository = usuarioRepository;
        this.usuarios = usuarios;
    }

    /** Identificadores de los escudos en el servicio público de football-data.org. */
    private static final Map<String, Integer> ESCUDOS = Map.ofEntries(
            Map.entry("Real Madrid", 86), Map.entry("FC Barcelona", 81),
            Map.entry("Atlético de Madrid", 78), Map.entry("Athletic Club", 77),
            Map.entry("Real Sociedad", 92), Map.entry("Villarreal CF", 94),
            Map.entry("Valencia CF", 95), Map.entry("Real Betis", 90),
            Map.entry("Sevilla FC", 559), Map.entry("Celta de Vigo", 558),
            Map.entry("CA Osasuna", 79), Map.entry("Getafe CF", 82),
            Map.entry("Rayo Vallecano", 87), Map.entry("RCD Espanyol", 80),
            Map.entry("Deportivo Alavés", 263), Map.entry("Elche CF", 285),
            Map.entry("Levante UD", 88), Map.entry("Málaga CF", 84),
            Map.entry("RC Deportivo", 560),
            Map.entry("Bayern de Múnich", 5), Map.entry("Borussia Dortmund", 4),
            Map.entry("RB Leipzig", 721), Map.entry("VfB Stuttgart", 10),
            Map.entry("Arsenal", 57), Map.entry("Aston Villa", 58),
            Map.entry("Liverpool", 64), Map.entry("Manchester City", 65),
            Map.entry("Manchester United", 66), Map.entry("Paris Saint-Germain", 524),
            Map.entry("LOSC Lille", 521), Map.entry("Inter de Milán", 108),
            Map.entry("Napoli", 113), Map.entry("AS Roma", 100),
            Map.entry("PSV Eindhoven", 674), Map.entry("Feyenoord", 675),
            Map.entry("FC Porto", 503), Map.entry("Sporting CP", 498),
            Map.entry("Club Brujas", 851), Map.entry("Galatasaray", 610));

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        usuario("creador@apuestas.es", "Creador de apuestas", "creador123", Rol.CREADOR);
        usuario("usuario@apuestas.es", "Usuario de prueba", "usuario123", Rol.USUARIO);

        Competicion laLiga = competicion("LaLiga", Deporte.FUTBOL);
        Competicion champions = competicion("Champions League", Deporte.FUTBOL);
        Competicion acb = competicion("Liga ACB", Deporte.BALONCESTO);
        Competicion euroliga = competicion("Euroliga", Deporte.BALONCESTO);
        Competicion f1 = competicion("Fórmula 1", Deporte.AUTOMOVILISMO);

        // Calificaciones orientativas (0-10): el creador de apuestas debe revisarlas
        equipo("Real Madrid", Deporte.FUTBOL, 6.7, laLiga, champions);
        equipo("FC Barcelona", Deporte.FUTBOL, 9.9, laLiga, champions);
        equipo("Atlético de Madrid", Deporte.FUTBOL, 8.7, laLiga, champions);
        equipo("Villarreal CF", Deporte.FUTBOL, 8.0, laLiga, champions);
        equipo("Athletic Club", Deporte.FUTBOL, 7.8, laLiga);
        equipo("Real Sociedad", Deporte.FUTBOL, 7.6, laLiga);
        equipo("Real Betis", Deporte.FUTBOL, 7.6, laLiga, champions);
        equipo("Celta de Vigo", Deporte.FUTBOL, 7.0, laLiga);
        equipo("Valencia CF", Deporte.FUTBOL, 6.8, laLiga);
        equipo("Sevilla FC", Deporte.FUTBOL, 6.8, laLiga);
        equipo("CA Osasuna", Deporte.FUTBOL, 6.5, laLiga);
        equipo("Rayo Vallecano", Deporte.FUTBOL, 6.5, laLiga);
        equipo("RCD Espanyol", Deporte.FUTBOL, 6.3, laLiga);
        equipo("Getafe CF", Deporte.FUTBOL, 6.2, laLiga);
        equipo("Deportivo Alavés", Deporte.FUTBOL, 6.2, laLiga);
        equipo("Elche CF", Deporte.FUTBOL, 6.0, laLiga);
        equipo("RC Deportivo", Deporte.FUTBOL, 5.9, laLiga);
        equipo("Levante UD", Deporte.FUTBOL, 5.8, laLiga);
        equipo("Málaga CF", Deporte.FUTBOL, 5.8, laLiga);
        equipo("Racing de Santander", Deporte.FUTBOL, 5.8, laLiga);
        equipo("Bayern de Múnich", Deporte.FUTBOL, 9.0, champions);
        equipo("Paris Saint-Germain", Deporte.FUTBOL, 9.3, champions);
        equipo("Arsenal", Deporte.FUTBOL, 9.2, champions);
        equipo("Manchester City", Deporte.FUTBOL, 9.0, champions);
        equipo("Liverpool", Deporte.FUTBOL, 9.0, champions);
        equipo("Inter de Milán", Deporte.FUTBOL, 8.8, champions);
        equipo("Napoli", Deporte.FUTBOL, 8.3, champions);
        equipo("Borussia Dortmund", Deporte.FUTBOL, 8.2, champions);
        equipo("Aston Villa", Deporte.FUTBOL, 8.0, champions);
        equipo("Manchester United", Deporte.FUTBOL, 7.8, champions);
        equipo("RB Leipzig", Deporte.FUTBOL, 7.8, champions);
        equipo("Sporting CP", Deporte.FUTBOL, 7.6, champions);
        equipo("FC Porto", Deporte.FUTBOL, 7.6, champions);
        equipo("AS Roma", Deporte.FUTBOL, 7.6, champions);
        equipo("PSV Eindhoven", Deporte.FUTBOL, 7.5, champions);
        equipo("Galatasaray", Deporte.FUTBOL, 7.5, champions);
        equipo("VfB Stuttgart", Deporte.FUTBOL, 7.4, champions);
        equipo("LOSC Lille", Deporte.FUTBOL, 7.3, champions);
        equipo("Feyenoord", Deporte.FUTBOL, 7.2, champions);
        equipo("Como", Deporte.FUTBOL, 7.2, champions);
        equipo("Fenerbahçe", Deporte.FUTBOL, 7.2, champions);
        equipo("RC Lens", Deporte.FUTBOL, 7.0, champions);
        equipo("Club Brujas", Deporte.FUTBOL, 7.0, champions);
        equipo("Shakhtar Donetsk", Deporte.FUTBOL, 6.8, champions);
        equipo("Bodø/Glimt", Deporte.FUTBOL, 6.8, champions);
        equipo("Slavia Praga", Deporte.FUTBOL, 6.7, champions);
        equipo("AEK Atenas", Deporte.FUTBOL, 6.5, champions);
        equipo("Slovan Bratislava", Deporte.FUTBOL, 6.0, champions);
        equipo("LASK", Deporte.FUTBOL, 6.0, champions);
        equipo("Viking FK", Deporte.FUTBOL, 5.8, champions);
        equipo("Sabah FK", Deporte.FUTBOL, 5.5, champions);
        equipo("Real Madrid Baloncesto", Deporte.BALONCESTO, 9.0, acb, euroliga);
        equipo("Barça Basket", Deporte.BALONCESTO, 8.5, acb, euroliga);
        equipo("Unicaja", Deporte.BALONCESTO, 7.5, acb);
        equipo("Max Verstappen", Deporte.AUTOMOVILISMO, 9.5, f1);
        equipo("Lance Stroll", Deporte.AUTOMOVILISMO, 2.0, f1);

        // LaLiga 2026/27, jornada 8
        partido(laLiga, "Málaga CF", "RCD Espanyol", 2026, 10, 9, 21, 0);
        partido(laLiga, "Rayo Vallecano", "Athletic Club", 2026, 10, 10, 14, 0);
        partido(laLiga, "Deportivo Alavés", "Atlético de Madrid", 2026, 10, 10, 16, 15);
        partido(laLiga, "FC Barcelona", "Getafe CF", 2026, 10, 10, 18, 30);
        partido(laLiga, "Real Madrid", "Villarreal CF", 2026, 10, 10, 21, 0);
        partido(laLiga, "Elche CF", "Celta de Vigo", 2026, 10, 11, 14, 0);
        partido(laLiga, "Real Sociedad", "RC Deportivo", 2026, 10, 11, 16, 15);
        partido(laLiga, "Real Betis", "CA Osasuna", 2026, 10, 11, 18, 30);
        partido(laLiga, "Racing de Santander", "Valencia CF", 2026, 10, 11, 21, 0);
        partido(laLiga, "Levante UD", "Sevilla FC", 2026, 10, 12, 21, 0);

        // LaLiga 2026/27, jornada 9 (partidos publicados hasta ahora)
        partido(laLiga, "RC Deportivo", "Levante UD", 2026, 10, 16, 21, 0);
        partido(laLiga, "RCD Espanyol", "Atlético de Madrid", 2026, 10, 17, 14, 0);
        partido(laLiga, "Villarreal CF", "Elche CF", 2026, 10, 17, 16, 15);
        partido(laLiga, "Real Betis", "FC Barcelona", 2026, 10, 17, 18, 30);
        partido(laLiga, "Valencia CF", "Athletic Club", 2026, 10, 17, 21, 0);
        partido(laLiga, "CA Osasuna", "Racing de Santander", 2026, 10, 18, 14, 0);

        // Escudos: solo se rellenan si el equipo aún no tiene uno (no pisa los cambios hechos en Gestión)
        ESCUDOS.forEach((nombre, id) -> equipos.findByNombre(nombre)
                .filter(e -> e.getEscudoUrl() == null)
                .ifPresent(e -> e.setEscudoUrl("https://crests.football-data.org/" + id + ".png")));

        // Champions League 2026/27, jornada 2
        partido(champions, "RC Lens", "Sporting CP", 2026, 10, 13, 18, 45);
        partido(champions, "Sabah FK", "Slavia Praga", 2026, 10, 13, 18, 45);
        partido(champions, "Galatasaray", "FC Barcelona", 2026, 10, 13, 21, 0);
        partido(champions, "RB Leipzig", "PSV Eindhoven", 2026, 10, 13, 21, 0);
        partido(champions, "Arsenal", "LOSC Lille", 2026, 10, 13, 21, 0);
        partido(champions, "Atlético de Madrid", "Manchester United", 2026, 10, 13, 21, 0);
        partido(champions, "Villarreal CF", "Napoli", 2026, 10, 13, 21, 0);
        partido(champions, "Viking FK", "Bayern de Múnich", 2026, 10, 13, 21, 0);
        partido(champions, "Inter de Milán", "Club Brujas", 2026, 10, 13, 21, 0);
        partido(champions, "LASK", "Liverpool", 2026, 10, 14, 18, 45);
        partido(champions, "Feyenoord", "Como", 2026, 10, 14, 18, 45);
        partido(champions, "Shakhtar Donetsk", "AEK Atenas", 2026, 10, 14, 21, 0);
        partido(champions, "Real Betis", "FC Porto", 2026, 10, 14, 21, 0);
        partido(champions, "AS Roma", "Real Madrid", 2026, 10, 14, 21, 0);
        partido(champions, "Slovan Bratislava", "VfB Stuttgart", 2026, 10, 14, 21, 0);
        partido(champions, "Aston Villa", "Fenerbahçe", 2026, 10, 14, 21, 0);
        partido(champions, "Manchester City", "Paris Saint-Germain", 2026, 10, 14, 21, 0);
        partido(champions, "Bodø/Glimt", "Borussia Dortmund", 2026, 10, 14, 21, 0);
    }

    private void usuario(String email, String nombre, String password, Rol rol) {
        if (!usuarioRepository.existsByEmail(email)) {
            usuarios.crear(email, nombre, password, rol);
        }
    }

    private Competicion competicion(String nombre, Deporte deporte) {
        return competiciones.findByNombre(nombre)
                .orElseGet(() -> competiciones.save(new Competicion(nombre, deporte)));
    }

    private void equipo(String nombre, Deporte deporte, double calidad, Competicion... enCompeticiones) {
        if (equipos.findByNombre(nombre).isPresent()) {
            return;
        }
        Equipo equipo = new Equipo(nombre, deporte, calidad);
        for (Competicion competicion : enCompeticiones) {
            equipo.participaEn(competicion);
        }
        equipos.save(equipo);
    }

    private void partido(Competicion competicion, String local, String visitante,
                         int anio, int mes, int dia, int hora, int minuto) {
        Equipo equipoLocal = equipos.findByNombre(local).orElseThrow();
        Equipo equipoVisitante = equipos.findByNombre(visitante).orElseThrow();
        LocalDateTime fechaHora = LocalDateTime.of(anio, mes, dia, hora, minuto);
        if (!eventos.existsByLocalAndVisitanteAndFechaHora(equipoLocal, equipoVisitante, fechaHora)) {
            eventos.save(new Evento(competicion, equipoLocal, equipoVisitante, fechaHora));
        }
    }
}
