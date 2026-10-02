package es.ucm.fdi.is1.apuestas.apuesta;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta y cambio de los límites de apuesta (HU-07). */
@Service
public class LimitesService {

    private final LimitesRepository limites;

    public LimitesService(LimitesRepository limites) {
        this.limites = limites;
    }

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

    @Transactional
    public Limites cambiar(BigDecimal importeMinimo, BigDecimal importeMaximo, int maxSelecciones) {
        Limites actuales = actuales();
        actuales.cambiar(importeMinimo, importeMaximo, maxSelecciones);
        return actuales;
    }
}
