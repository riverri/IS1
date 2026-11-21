package es.ucm.fdi.is1.apuestas.apuesta;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.web.Csv;

/**
 * Descarga del historial en CSV (HU-114): mis apuestas y los movimientos de mi saldo. Solo los datos del usuario con
 * sesión; sin sesión, {@code SeguridadConfig} lleva al login.
 */
@Controller
public class ExportacionController {

    private final ApuestaService apuestas;
    private final MovimientoSaldoRepository movimientos;
    private final UsuarioRepository usuarios;

    public ExportacionController(ApuestaService apuestas, MovimientoSaldoRepository movimientos,
                                 UsuarioRepository usuarios) {
        this.apuestas = apuestas;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
    }

    /** GET /apuestas/exportar: todas mis apuestas, de la más reciente a la más antigua (CA-114.1). */
    @GetMapping("/apuestas/exportar")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> apuestas(Principal principal) {
        List<Apuesta> todas = new ArrayList<>(apuestas.activas(principal.getName()));
        todas.addAll(apuestas.historial(principal.getName()));
        todas.sort(Comparator.comparing(Apuesta::getFecha).reversed());
        Csv csv = new Csv().fila("Fecha", "Apuesta", "Selecciones", "Importe", "Cuota", "Estado", "Cobrado");
        for (Apuesta a : todas) {
            csv.fila(a.getFecha(), a.descripcion(), a.getSelecciones().size(), a.getImporte(), a.getCuota(),
                    a.getEstado().name().toLowerCase(Locale.ROOT), a.getPagado());
        }
        return csv.descarga("mis-apuestas.csv");
    }

    /** GET /cuenta/movimientos/exportar: cada cambio de mi saldo con su motivo y el saldo que dejó (CA-114.2). */
    @GetMapping("/cuenta/movimientos/exportar")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> movimientos(Principal principal) {
        Csv csv = new Csv().fila("Fecha", "Tipo", "Concepto", "Importe", "Saldo");
        for (MovimientoSaldo m : movimientos.findByUsuarioOrderByIdAsc(usuarios.findByEmail(principal.getName()).orElseThrow())) {
            csv.fila(m.getFecha(), m.getTipo().getDescripcion(), m.getConcepto(), m.getImporte(), m.getSaldoResultante());
        }
        return csv.descarga("mis-movimientos.csv");
    }
}
