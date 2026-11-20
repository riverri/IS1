package es.ucm.fdi.is1.apuestas.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.is1.apuestas.apuesta.SupercuotaService;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.EquiposService;
import es.ucm.fdi.is1.apuestas.eventos.CatalogoService;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.FichaEquipoService;
import es.ucm.fdi.is1.apuestas.seguimiento.FavoritosService;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoProperties;

/**
 * Controlador de las páginas públicas generales: la portada y el listado y la ficha de los equipos.
 * Se ven sin haber iniciado sesión (HU-08; las rutas públicas están en {@code SeguridadConfig}).
 */
@Controller
public class InicioController {

    private final EquiposService equipos;
    private final CatalogoService catalogo;
    private final FichaEquipoService fichas;
    private final SaldoProperties saldo;
    private final FavoritosService favoritos;
    private final SupercuotaService supercuotas;

    public InicioController(EquiposService equipos, CatalogoService catalogo, FichaEquipoService fichas,
                            SaldoProperties saldo,
                            FavoritosService favoritos, SupercuotaService supercuotas) {
        this.equipos = equipos;
        this.catalogo = catalogo;
        this.fichas = fichas;
        this.saldo = saldo;
        this.favoritos = favoritos;
        this.supercuotas = supercuotas;
    }

    /**
     * GET /: portada con los 6 próximos partidos, el partido destacado, los eventos disponibles de cada deporte
     * y el saldo de bienvenida que se recibe al registrarse (HU-08, HU-14).
     */
    @GetMapping("/")
    public String inicio(Model model) {
        List<Evento> proximos = catalogo.proximos(6);
        Evento destacado = catalogo.destacado();
        model.addAttribute("proximos", proximos);
        model.addAttribute("destacado", destacado);
        model.addAttribute("recuento", catalogo.recuentoPorDeporte());
        model.addAttribute("bienvenida", saldo.bienvenida());
        model.addAttribute("supercuotas", supercuotas.vigentes());
        List<Evento> partidos = new ArrayList<>(proximos);
        if (destacado != null) {
            partidos.add(destacado);
        }
        model.addAttribute("cuotasPorEvento", supercuotas.cuotasConSupercuota(partidos));
        return "index";
    }

    /**
     * GET /equipos: equipos y deportistas agrupados por deporte. Filtros opcionales: deporte, competición
     * y texto {@code q} en el nombre.
     */
    @GetMapping("/equipos")
    public String equipos(@RequestParam(required = false) Deporte deporte,
                          @RequestParam(required = false) Long competicion,
                          @RequestParam(required = false) String q, Model model) {
        model.addAttribute("equiposPorDeporte", equipos.buscar(deporte, competicion, q));
        model.addAttribute("recuento", equipos.recuentoPorDeporte());
        model.addAttribute("deporteElegido", deporte);
        model.addAttribute("competicionElegida", competicion);
        // El desplegable de competiciones solo aparece con un deporte elegido (List.of() = lista vacía)
        model.addAttribute("competiciones", deporte == null ? List.of() : equipos.competiciones(deporte));
        model.addAttribute("q", q == null ? "" : q.trim());
        return "equipos";
    }

    /** GET /equipos/{id}: ficha pública de un equipo o deportista (HU-31). */
    @GetMapping("/equipos/{id}")
    public String ficha(@PathVariable Long id, Principal principal, Model model) {
        var ficha = fichas.ficha(id);
        model.addAttribute("ficha", ficha);
        model.addAttribute("cuotasPorEvento", supercuotas.cuotasConSupercuota(ficha.proximos()));
        // HU-86: si lo sigo, el botón dice «Quitar de favoritos»
        model.addAttribute("favorito", principal != null && favoritos.esFavorito(principal.getName(), id));
        return "equipo";
    }
}
