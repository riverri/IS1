package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
 * Páginas de las criptomonedas (HU-69 a HU-72). La lista y cada cripto son públicas (HU-69); comprar, vender y la
 * cartera exigen sesión ({@code SeguridadConfig}). La lógica está en {@link CriptoService}.
 */
@Controller
@RequestMapping("/cripto")
public class CriptoController {

    private final CriptoService cripto;

    public CriptoController(CriptoService cripto) {
        this.cripto = cripto;
    }

    /** GET /cripto: las memecoins primero, con la que más se ha movido hoy destacada (CA-69.1). */
    @GetMapping
    public String lista(Model model) {
        List<Criptomoneda> criptos = cripto.criptos();
        model.addAttribute("memecoins", criptos.stream()
                .filter(c -> c.getCategoria() == CategoriaCripto.MEMECOIN).toList());
        model.addAttribute("referencia", criptos.stream()
                .filter(c -> c.getCategoria() == CategoriaCripto.REFERENCIA).toList());
        // La que más ha subido o bajado en 24 horas, sea cual sea el signo
        model.addAttribute("destacada", criptos.stream()
                .filter(c -> c.getVariacion24h() != null)
                .max(Comparator.comparing(c -> c.getVariacion24h().abs()))
                .orElse(null));
        model.addAttribute("categorias", CategoriaCripto.values());
        model.addAttribute("configuracion", cripto.configuracion());
        return "cripto/lista";
    }

    /** GET /cripto/cartera: lo que tengo, cuánto vale y mis últimas operaciones (HU-72). */
    @GetMapping("/cartera")
    public String cartera(Principal principal, Model model) {
        model.addAttribute("cartera", cripto.cartera(principal.getName()));
        model.addAttribute("operaciones", cripto.operaciones(principal.getName()));
        return "cripto/cartera";
    }

    /**
     * GET /cripto/{simbolo}: precio, gráfico de la semana y, con sesión, lo que tengo. Con {@code importe}, enseña antes
     * de comprar cuánta cripto se recibiría (CA-70.1).
     */
    @GetMapping("/{simbolo}")
    public String cripto(@PathVariable String simbolo, @RequestParam(required = false) BigDecimal importe,
                         Principal principal, Model model) {
        Criptomoneda moneda = cripto.cripto(simbolo);
        model.addAttribute("cripto", moneda);
        model.addAttribute("grafico", cripto.grafico(simbolo));
        model.addAttribute("alDia", cripto.precioAlDia(moneda));
        model.addAttribute("configuracion", cripto.configuracion());
        if (principal != null) {
            model.addAttribute("posicion", cripto.posicion(principal.getName(), simbolo));
        }
        if (importe != null && importe.signum() > 0 && importe.stripTrailingZeros().scale() <= 2) {
            model.addAttribute("cotizacion", cripto.cotizarCompra(simbolo, importe));
        }
        return "cripto/cripto";
    }

    /** POST /cripto/{simbolo}/comprar: compra con el precio que se veía (CA-70.1 a CA-70.4). */
    @PostMapping("/{simbolo}/comprar")
    public String comprar(@PathVariable String simbolo, @RequestParam(required = false) BigDecimal importe,
                          @RequestParam(required = false) BigDecimal precioVisto, Principal principal,
                          RedirectAttributes redireccion) {
        String volver = "redirect:/cripto/" + simbolo.toUpperCase();
        try {
            PosicionCripto posicion = cripto.comprar(principal.getName(), simbolo, importe, precioVisto);
            redireccion.addFlashAttribute("mensaje", "Compra hecha. Ahora tienes "
                    + texto(posicion.getCantidad()) + " " + posicion.getCripto().getSimbolo() + ".");
        } catch (PrecioCambiadoException e) {
            // Se vuelve a la previsión con el precio nuevo, para que decida otra vez
            redireccion.addFlashAttribute("aviso", e.getMessage());
            return volver + "?importe=" + Objects.requireNonNull(importe).toPlainString();
        } catch (SaldoInsuficienteException e) {
            redireccion.addFlashAttribute("error", "No tienes saldo suficiente para esta compra");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return volver;
    }

    /** POST /cripto/{simbolo}/vender: vende una parte o todo (CA-71.1, CA-71.2). */
    @PostMapping("/{simbolo}/vender")
    public String vender(@PathVariable String simbolo, @RequestParam(required = false) BigDecimal cantidad,
                         @RequestParam(defaultValue = "false") boolean todo,
                         @RequestParam(required = false) BigDecimal precioVisto, Principal principal,
                         RedirectAttributes redireccion) {
        try {
            BigDecimal recibido = cripto.vender(principal.getName(), simbolo, todo ? null : cantidad, precioVisto);
            redireccion.addFlashAttribute("mensaje", "Venta hecha. Recibes " + texto(recibido) + " monedas.");
        } catch (PrecioCambiadoException e) {
            redireccion.addFlashAttribute("aviso", e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cripto/" + simbolo.toUpperCase();
    }

    private static String texto(BigDecimal cantidad) {
        return cantidad.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
