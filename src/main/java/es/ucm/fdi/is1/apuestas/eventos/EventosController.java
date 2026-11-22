package es.ucm.fdi.is1.apuestas.eventos;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.is1.apuestas.apuesta.SupercuotaService;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.seguimiento.FavoritosService;

/**
 * Controlador MVC del catálogo público de eventos (HU-19, HU-22). Delega en {@link CatalogoService}
 * y pinta la plantilla Thymeleaf {@code eventos.html}.
 */
@Controller
public class EventosController {

    private final CatalogoService catalogo;
    private final FavoritosService favoritos;
    private final SupercuotaService supercuotas;

    public EventosController(CatalogoService catalogo, FavoritosService favoritos, SupercuotaService supercuotas) {
        this.catalogo = catalogo;
        this.favoritos = favoritos;
        this.supercuotas = supercuotas;
    }

    /**
     * {@code GET /eventos}: eventos disponibles agrupados por deporte y competición, con pestañas por
     * deporte y buscador.
     *
     * @param deporte pestaña elegida ({@code ?deporte=FUTBOL}); sin él se muestran todos
     * @param q       texto buscado en equipos y competición ({@code ?q=madrid}); opcional
     */
    @GetMapping("/eventos")
    public String catalogo(@RequestParam(required = false) Deporte deporte,
                           @RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "0") int pagina, Principal principal,
                           Model model) {
        // HU-86: los próximos partidos de mis equipos, arriba del todo (sin filtros ni búsqueda)
        List<Evento> deMisEquipos = principal == null || deporte != null || (q != null && !q.isBlank())
                ? List.of() : favoritos.proximosPartidos(principal.getName());
        model.addAttribute("deMisEquipos", deMisEquipos);
        // Spring convierte el texto "FUTBOL" de la URL en el valor Deporte.FUTBOL del enum automáticamente
        var paginaCatalogo = catalogo.buscar(deporte, q, pagina);
        var eventosPorDeporte = paginaCatalogo.eventos();
        model.addAttribute("eventosPorDeporte", eventosPorDeporte);
        model.addAttribute("pagina", paginaCatalogo);
        List<Evento> visibles = new ArrayList<>(deMisEquipos);
        eventosPorDeporte.values().forEach(competiciones ->
                competiciones.values().forEach(visibles::addAll));
        model.addAttribute("cuotasPorEvento", supercuotas.cuotasConSupercuota(visibles));
        // El recuento de las pestañas no depende de los filtros: siempre se ven todos los deportes
        model.addAttribute("recuento", catalogo.recuentoPorDeporte());
        model.addAttribute("deporteElegido", deporte);
        model.addAttribute("q", q == null ? "" : q.trim());
        return "eventos";
    }
}
