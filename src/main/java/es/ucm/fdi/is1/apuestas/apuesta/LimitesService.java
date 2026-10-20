package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta y cambio de los límites de apuesta (HU-07) y del interruptor de juego responsable (HU-10).
 * Lo usan los servicios de apuestas, el panel de gestión y las páginas que muestran el importe mínimo y máximo.
 */
@Service
public class LimitesService {

    private final LimitesRepository limites;

    public LimitesService(LimitesRepository limites) {
        this.limites = limites;
    }

    /**
     * La única fila de límites.
     *
     * @throws IllegalStateException si falta (la base de datos no tiene aplicadas las migraciones)
     */
    @Transactional(readOnly = true)
    public Limites actuales() {
        return limites.findById(Limites.ID)
                .orElseThrow(() -> new IllegalStateException("Faltan los límites de apuesta (migración V3)"));
    }

    /** Activa o desactiva el juego responsable en toda la web (HU-10). */
    @Transactional
    public void activarJuegoResponsable(boolean activo) {
        actuales().activarJuegoResponsable(activo);
    }

    /**
     * Cambia los límites (HU-07). Las apuestas ya hechas no se tocan: los límites se comprueban al apostar.
     *
     * @throws IllegalArgumentException si los valores no son coherentes (ver {@link Limites#cambiar})
     */
    @Transactional
    public Limites cambiar(BigDecimal importeMinimo, BigDecimal importeMaximo, int maxSelecciones) {
        Limites actuales = actuales();
        // Sin save: la entidad está gestionada y JPA guarda el cambio al terminar la transacción
        actuales.cambiar(importeMinimo, importeMaximo, maxSelecciones);
        return actuales;
    }
}
