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
        Competicion nba = competicion("NBA", Deporte.BALONCESTO);
        Competicion atp = competicion("ATP Masters 1000", Deporte.TENIS);
        Competicion motoGp = competicion("MotoGP", Deporte.MOTOCICLISMO);

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
        equipo("Valencia Basket", Deporte.BALONCESTO, 8.0, acb, euroliga);
        equipo("Baskonia", Deporte.BALONCESTO, 7.6, acb, euroliga);
        equipo("Gran Canaria", Deporte.BALONCESTO, 7.0, acb);
        equipo("Joventut Badalona", Deporte.BALONCESTO, 6.8, acb);
        equipo("UCAM Murcia", Deporte.BALONCESTO, 6.9, acb);
        equipo("Olympiacos", Deporte.BALONCESTO, 8.8, euroliga);
        equipo("Panathinaikos", Deporte.BALONCESTO, 8.9, euroliga);
        equipo("Fenerbahçe Beko", Deporte.BALONCESTO, 8.9, euroliga);
        equipo("Zalgiris Kaunas", Deporte.BALONCESTO, 7.4, euroliga);
        equipo("Oklahoma City Thunder", Deporte.BALONCESTO, 9.6, nba);
        equipo("Denver Nuggets", Deporte.BALONCESTO, 9.0, nba);
        equipo("New York Knicks", Deporte.BALONCESTO, 8.8, nba);
        equipo("Boston Celtics", Deporte.BALONCESTO, 8.6, nba);
        equipo("Los Angeles Lakers", Deporte.BALONCESTO, 8.5, nba);
        equipo("Golden State Warriors", Deporte.BALONCESTO, 8.3, nba);
        equipo("San Antonio Spurs", Deporte.BALONCESTO, 8.0, nba);
        equipo("Dallas Mavericks", Deporte.BALONCESTO, 7.8, nba);
        equipo("Carlos Alcaraz", Deporte.TENIS, 9.8, atp);
        equipo("Jannik Sinner", Deporte.TENIS, 9.7, atp);
        equipo("Novak Djokovic", Deporte.TENIS, 8.9, atp);
        equipo("Alexander Zverev", Deporte.TENIS, 8.6, atp);
        equipo("Taylor Fritz", Deporte.TENIS, 8.2, atp);
        equipo("Jack Draper", Deporte.TENIS, 8.0, atp);
        equipo("Holger Rune", Deporte.TENIS, 7.6, atp);
        equipo("Alejandro Davidovich", Deporte.TENIS, 7.4, atp);
        equipo("Max Verstappen", Deporte.AUTOMOVILISMO, 9.5, f1);
        equipo("Lance Stroll", Deporte.AUTOMOVILISMO, 2.0, f1);
        equipo("Lando Norris", Deporte.AUTOMOVILISMO, 9.3, f1);
        equipo("Oscar Piastri", Deporte.AUTOMOVILISMO, 9.1, f1);
        equipo("George Russell", Deporte.AUTOMOVILISMO, 8.8, f1);
        equipo("Charles Leclerc", Deporte.AUTOMOVILISMO, 8.7, f1);
        equipo("Lewis Hamilton", Deporte.AUTOMOVILISMO, 8.2, f1);
        equipo("Fernando Alonso", Deporte.AUTOMOVILISMO, 7.6, f1);
        equipo("Carlos Sainz", Deporte.AUTOMOVILISMO, 7.5, f1);
        equipo("Marc Márquez", Deporte.MOTOCICLISMO, 9.8, motoGp);
        equipo("Álex Márquez", Deporte.MOTOCICLISMO, 8.6, motoGp);
        equipo("Francesco Bagnaia", Deporte.MOTOCICLISMO, 8.5, motoGp);
        equipo("Pedro Acosta", Deporte.MOTOCICLISMO, 8.2, motoGp);
        equipo("Marco Bezzecchi", Deporte.MOTOCICLISMO, 8.1, motoGp);
        equipo("Jorge Martín", Deporte.MOTOCICLISMO, 7.9, motoGp);

        // LaLiga 2026/27, jornada 8
        partido(laLiga, "Jornada 8", "Málaga CF", "RCD Espanyol", 2026, 10, 9, 21, 0);
        partido(laLiga, "Jornada 8", "Rayo Vallecano", "Athletic Club", 2026, 10, 10, 14, 0);
        partido(laLiga, "Jornada 8", "Deportivo Alavés", "Atlético de Madrid", 2026, 10, 10, 16, 15);
        partido(laLiga, "Jornada 8", "FC Barcelona", "Getafe CF", 2026, 10, 10, 18, 30);
        partido(laLiga, "Jornada 8", "Real Madrid", "Villarreal CF", 2026, 10, 10, 21, 0);
        partido(laLiga, "Jornada 8", "Elche CF", "Celta de Vigo", 2026, 10, 11, 14, 0);
        partido(laLiga, "Jornada 8", "Real Sociedad", "RC Deportivo", 2026, 10, 11, 16, 15);
        partido(laLiga, "Jornada 8", "Real Betis", "CA Osasuna", 2026, 10, 11, 18, 30);
        partido(laLiga, "Jornada 8", "Racing de Santander", "Valencia CF", 2026, 10, 11, 21, 0);
        partido(laLiga, "Jornada 8", "Levante UD", "Sevilla FC", 2026, 10, 12, 21, 0);

        // LaLiga 2026/27, jornada 9 (partidos publicados hasta ahora)
        partido(laLiga, "Jornada 9", "RC Deportivo", "Levante UD", 2026, 10, 16, 21, 0);
        partido(laLiga, "Jornada 9", "RCD Espanyol", "Atlético de Madrid", 2026, 10, 17, 14, 0);
        partido(laLiga, "Jornada 9", "Villarreal CF", "Elche CF", 2026, 10, 17, 16, 15);
        partido(laLiga, "Jornada 9", "Real Betis", "FC Barcelona", 2026, 10, 17, 18, 30);
        partido(laLiga, "Jornada 9", "Valencia CF", "Athletic Club", 2026, 10, 17, 21, 0);
        partido(laLiga, "Jornada 9", "CA Osasuna", "Racing de Santander", 2026, 10, 18, 14, 0);

        eventosDeEjemplo(acb, euroliga, nba, atp, f1, motoGp);

        // Escudos: solo se rellenan si el equipo aún no tiene uno (no pisa los cambios hechos en Gestión)
        // y se guarda el identificador de football-data.org para enlazar con la API
        ESCUDOS.forEach((nombre, id) -> equipos.findByNombre(nombre).ifPresent(e -> {
            if (e.getEscudoUrl() == null) {
                e.setEscudoUrl("https://crests.football-data.org/" + id + ".png");
            }
            if (e.getIdExterno() == null && equipos.findByIdExterno(id).isEmpty()) {
                e.setIdExterno(id);
            }
        }));

        // Champions League 2026/27, jornada 2
        partido(champions, "Fase de liga · Jornada 2", "RC Lens", "Sporting CP", 2026, 10, 13, 18, 45);
        partido(champions, "Fase de liga · Jornada 2", "Sabah FK", "Slavia Praga", 2026, 10, 13, 18, 45);
        partido(champions, "Fase de liga · Jornada 2", "Galatasaray", "FC Barcelona", 2026, 10, 13, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "RB Leipzig", "PSV Eindhoven", 2026, 10, 13, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Arsenal", "LOSC Lille", 2026, 10, 13, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Atlético de Madrid", "Manchester United", 2026, 10, 13, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Villarreal CF", "Napoli", 2026, 10, 13, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Viking FK", "Bayern de Múnich", 2026, 10, 13, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Inter de Milán", "Club Brujas", 2026, 10, 13, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "LASK", "Liverpool", 2026, 10, 14, 18, 45);
        partido(champions, "Fase de liga · Jornada 2", "Feyenoord", "Como", 2026, 10, 14, 18, 45);
        partido(champions, "Fase de liga · Jornada 2", "Shakhtar Donetsk", "AEK Atenas", 2026, 10, 14, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Real Betis", "FC Porto", 2026, 10, 14, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "AS Roma", "Real Madrid", 2026, 10, 14, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Slovan Bratislava", "VfB Stuttgart", 2026, 10, 14, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Aston Villa", "Fenerbahçe", 2026, 10, 14, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Manchester City", "Paris Saint-Germain", 2026, 10, 14, 21, 0);
        partido(champions, "Fase de liga · Jornada 2", "Bodø/Glimt", "Borussia Dortmund", 2026, 10, 14, 21, 0);
    }

    /**
     * Eventos de ejemplo de otros deportes, para tener el catálogo completo por categorías.
     * No son el calendario real: el creador de apuestas debe sustituirlos por los reales.
     */
    private void eventosDeEjemplo(Competicion acb, Competicion euroliga, Competicion nba,
                                  Competicion atp, Competicion f1, Competicion motoGp) {
        partido(acb, "Jornada 4", "Real Madrid Baloncesto", "Valencia Basket", 2026, 10, 4, 18, 0);
        partido(acb, "Jornada 4", "Barça Basket", "Baskonia", 2026, 10, 4, 20, 30);
        partido(acb, "Jornada 4", "Unicaja", "Gran Canaria", 2026, 10, 5, 12, 30);
        partido(acb, "Jornada 4", "Joventut Badalona", "UCAM Murcia", 2026, 10, 5, 17, 0);
        partido(euroliga, "Jornada 3", "Olympiacos", "Real Madrid Baloncesto", 2026, 10, 8, 19, 15);
        partido(euroliga, "Jornada 3", "Barça Basket", "Panathinaikos", 2026, 10, 8, 20, 30);
        partido(euroliga, "Jornada 3", "Fenerbahçe Beko", "Valencia Basket", 2026, 10, 9, 18, 45);
        partido(euroliga, "Jornada 3", "Baskonia", "Zalgiris Kaunas", 2026, 10, 9, 20, 30);
        partido(nba, "Temporada regular", "Los Angeles Lakers", "Golden State Warriors", 2026, 10, 21, 4, 0);
        partido(nba, "Temporada regular", "Boston Celtics", "New York Knicks", 2026, 10, 22, 1, 30);
        partido(nba, "Temporada regular", "Oklahoma City Thunder", "Denver Nuggets", 2026, 10, 22, 2, 0);
        partido(nba, "Temporada regular", "San Antonio Spurs", "Dallas Mavericks", 2026, 10, 22, 2, 30);
        partido(atp, "Shanghái · Cuartos de final", "Carlos Alcaraz", "Jack Draper", 2026, 10, 9, 9, 30);
        partido(atp, "Shanghái · Cuartos de final", "Jannik Sinner", "Holger Rune", 2026, 10, 9, 12, 0);
        partido(atp, "Shanghái · Cuartos de final", "Novak Djokovic", "Taylor Fritz", 2026, 10, 9, 14, 30);
        partido(atp, "Shanghái · Cuartos de final", "Alexander Zverev", "Alejandro Davidovich", 2026, 10, 9, 7, 0);
        partido(f1, "GP de Singapur · Duelos", "Fernando Alonso", "Carlos Sainz", 2026, 10, 11, 14, 0);
        partido(f1, "GP de Singapur · Duelos", "Charles Leclerc", "Lewis Hamilton", 2026, 10, 11, 14, 0);
        partido(f1, "GP de Singapur · Duelos", "Lando Norris", "Oscar Piastri", 2026, 10, 11, 14, 0);
        partido(f1, "GP de Singapur · Duelos", "Max Verstappen", "George Russell", 2026, 10, 11, 14, 0);
        partido(motoGp, "GP de Australia · Duelos", "Marc Márquez", "Francesco Bagnaia", 2026, 10, 18, 5, 0);
        partido(motoGp, "GP de Australia · Duelos", "Jorge Martín", "Pedro Acosta", 2026, 10, 18, 5, 0);
        partido(motoGp, "GP de Australia · Duelos", "Álex Márquez", "Marco Bezzecchi", 2026, 10, 18, 5, 0);
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

    private void partido(Competicion competicion, String fase, String local, String visitante,
                         int anio, int mes, int dia, int hora, int minuto) {
        Equipo equipoLocal = equipos.findByNombre(local).orElseThrow();
        Equipo equipoVisitante = equipos.findByNombre(visitante).orElseThrow();
        LocalDateTime fechaHora = LocalDateTime.of(anio, mes, dia, hora, minuto);
        Evento evento = eventos.findByLocalAndVisitanteAndFechaHora(equipoLocal, equipoVisitante, fechaHora)
                .orElseGet(() -> eventos.save(new Evento(competicion, equipoLocal, equipoVisitante, fechaHora)));
        if (evento.getFase() == null) {
            evento.setFase(fase);
        }
    }
}
