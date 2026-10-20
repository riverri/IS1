package es.ucm.fdi.is1.apuestas.api;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la API de datos deportivos (application.properties, prefijo apuestas.api).
 * Spring la rellena al arrancar gracias a {@code @ConfigurationPropertiesScan}. Los intervalos de las tareas
 * programadas no están aquí: los lee directamente {@link SincronizacionProgramada}.
 *
 * @param url           dirección base de la API v4 de football-data.org
 * @param token         clave personal; se lee de la variable de entorno FOOTBALL_DATA_TOKEN y nunca se sube a Git
 * @param competiciones códigos de las competiciones a sincronizar (PD = LaLiga, CL = Champions League…)
 * @param competicionesPlantillas ligas de las que se descargan las plantillas. En el plan gratuito la Champions
 *                      no trae jugadores, así que se usan las ligas de cada país; si falta, las de {@code competiciones}
 * @param diasAtras     días hacia atrás en los que se buscan resultados
 * @param diasAdelante  días hacia delante en los que se buscan partidos
 * @param zonaHoraria   zona horaria en la que se guardan las fechas de los partidos
 */
@ConfigurationProperties("apuestas.api")
public record ApiProperties(String url, String token, List<String> competiciones,
                            List<String> competicionesPlantillas, int diasAtras, int diasAdelante,
                            String zonaHoraria) {

    /** Ligas de las que se descargan las plantillas: {@code competicionesPlantillas} o, si no hay, {@code competiciones}. */
    public List<String> ligasDePlantillas() {
        return competicionesPlantillas == null || competicionesPlantillas.isEmpty()
                ? competiciones : competicionesPlantillas;
    }

    /**
     * Si hay clave de la API. Sin ella no se sincroniza nada: las tareas programadas no hacen nada y los botones
     * de Gestión muestran un aviso.
     */
    public boolean configurada() {
        return token != null && !token.isBlank();
    }
}
