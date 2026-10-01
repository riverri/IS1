package es.ucm.fdi.is1.apuestas.web;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.ucm.fdi.is1.apuestas.apuesta.Boleto;
import es.ucm.fdi.is1.apuestas.apuesta.BoletoService;
import es.ucm.fdi.is1.apuestas.apuesta.BoletoVista;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Datos comunes a todas las vistas: usuario con sesión iniciada (cabecera, saldo) y su boleto. */
@ControllerAdvice
public class UsuarioActualAdvice {

    private final UsuarioService usuarios;
    private final Boleto boleto;
    private final BoletoService boletos;

    public UsuarioActualAdvice(UsuarioService usuarios, Boleto boleto, BoletoService boletos) {
        this.usuarios = usuarios;
        this.boleto = boleto;
        this.boletos = boletos;
    }

    /** El boleto en construcción, para marcar las cuotas elegidas en el catálogo. */
    @ModelAttribute("boleto")
    public Boleto boleto() {
        return boleto;
    }

    /** Resumen para la barra del boleto: número de selecciones y multiplicador. Null si está vacío. */
    @ModelAttribute("boletoResumen")
    public BoletoVista boletoResumen(Principal principal) {
        if (principal == null || boleto.isVacio()) {
            return null;
        }
        return boletos.vista(boleto);
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarios.buscar(principal.getName()).orElse(null);
    }
}
