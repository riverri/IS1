package es.ucm.fdi.is1.apuestas.prediccion;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;

/**
 * Páginas de los mercados de predicción (HU-64 a HU-68). Ver la lista y cada mercado es público (HU-67); comprar y
 * vender exige sesión ({@code SeguridadConfig}). La lógica está en {@link PrediccionService}.
 */
@Controller
@RequestMapping("/predicciones")
public class PrediccionController {

    private final PrediccionService predicciones;
    private final Clock reloj;

    public PrediccionController(PrediccionService predicciones, Clock reloj) {
        this.predicciones = predicciones;
        this.reloj = reloj;
    }

    /** GET /predicciones: los abiertos y los ya cerrados o resueltos, con su precio. */
    @GetMapping
    public String lista(Model model) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<MercadoPrediccion> todos = predicciones.todos();
        model.addAttribute("abiertos", todos.stream().filter(m -> m.admiteOperaciones(ahora)).toList());
        model.addAttribute("terminados", todos.stream()
                .filter(m -> !m.admiteOperaciones(ahora) && m.getEstado() != EstadoPrediccion.ANULADO).toList());
        return "predicciones";
    }

    /**
     * GET /predicciones/{id}: precio, gráfico y estadísticas (HU-67); con sesión, también mi posición y la de mis
     * compañeros de liga (HU-68). Con {@code lado} e {@code importe}, enseña antes de comprar cuántas participaciones
     * se recibirían (CA-64.1).
     */
    @GetMapping("/{id}")
    public String mercado(@PathVariable Long id, @RequestParam(required = false) Lado lado,
                          @RequestParam(required = false) BigDecimal importe, Principal principal, Model model) {
        MercadoPrediccion mercado = predicciones.mercado(id);
        model.addAttribute("mercado", mercado);
        model.addAttribute("abierto", mercado.admiteOperaciones(LocalDateTime.now(reloj)));
        model.addAttribute("estadisticas", predicciones.estadisticas(id));
        model.addAttribute("lados", Lado.values());
        if (principal != null) {
            model.addAttribute("mias", predicciones.mias(principal.getName(), id));
            model.addAttribute("deMisLigas", predicciones.deMisLigas(principal.getName(), id));
        }
        if (lado != null && importe != null && importe.signum() > 0) {
            model.addAttribute("cotizacion", predicciones.cotizarCompra(id, lado, importe));
        }
        return "prediccion";
    }

    /** POST /predicciones/{id}/comprar: compra con el precio que se veía (CA-64.1 a CA-64.3). */
    @PostMapping("/{id}/comprar")
    public String comprar(@PathVariable Long id, @RequestParam(required = false) Lado lado,
                          @RequestParam(required = false) BigDecimal importe,
                          @RequestParam(required = false) BigDecimal precioVisto, Principal principal,
                          RedirectAttributes redireccion) {
        try {
            PosicionPrediccion posicion = predicciones.comprar(principal.getName(), id, lado, importe, precioVisto);
            redireccion.addFlashAttribute("mensaje", "Compra hecha. Tienes " + texto(posicion.getParticipaciones())
                    + " participaciones de «" + lado.getNombre() + "».");
            return "redirect:/predicciones/" + id;
        } catch (PrecioCambiadoException e) {
            // Se vuelve a la previsión con el precio nuevo, para que decida otra vez
            redireccion.addFlashAttribute("aviso", e.getMessage());
            return "redirect:/predicciones/" + id + "?lado=" + lado + "&importe=" + importe.toPlainString();
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para esta compra");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/predicciones/" + id;
    }

    /** POST /predicciones/{id}/vender: vende participaciones (CA-65.1, CA-65.2). */
    @PostMapping("/{id}/vender")
    public String vender(@PathVariable Long id, @RequestParam(required = false) Lado lado,
                         @RequestParam(required = false) BigDecimal participaciones,
                         @RequestParam(required = false) BigDecimal precioVisto, Principal principal,
                         RedirectAttributes redireccion) {
        try {
            BigDecimal recibido = predicciones.vender(principal.getName(), id, lado, participaciones, precioVisto);
            redireccion.addFlashAttribute("mensaje", "Venta hecha. Recibes " + texto(recibido) + " monedas.");
        } catch (PrecioCambiadoException e) {
            redireccion.addFlashAttribute("aviso", e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/predicciones/" + id;
    }

    private static String texto(BigDecimal cantidad) {
        return cantidad.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
