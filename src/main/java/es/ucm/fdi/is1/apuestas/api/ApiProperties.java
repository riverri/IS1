package es.ucm.fdi.is1.apuestas.api;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la API de datos deportivos (application.properties, prefijo apuestas.api).
 *
 * @param url           dirección base de la API v4 de football-data.org
 * @param token         clave personal; se lee de la variable de entorno FOOTBALL_DATA_TOKEN y nunca se sube a Git
 * @param competiciones códigos de las competiciones a sincronizar (PD = LaLiga, CL = Champions League…)
 * @param diasAtras     días hacia atrás en los que se buscan resultados
 * @param diasAdelante  días hacia delante en los que se buscan partidos
 * @param zonaHoraria   zona horaria en la que se guardan las fechas de los partidos
 */
@ConfigurationProperties("apuestas.api")
public record ApiProperties(String url, String token, List<String> competiciones, int diasAtras, int diasAdelante,
                            String zonaHoraria) {

    public boolean configurada() {
        return token != null && !token.isBlank();
    }
}
