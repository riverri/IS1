package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de las moneditas (application.properties, prefijo apuestas.saldo) para el saldo
 * de bienvenida y las recargas gratuitas (HU-14). Spring crea este record con esos valores.
 *
 * @param bienvenida     saldo inicial al registrarse
 * @param recargaImporte importe de cada recarga gratuita
 * @param recargaPeriodo tiempo mínimo entre recargas gratuitas
 */
@ConfigurationProperties("apuestas.saldo")
public record SaldoProperties(BigDecimal bienvenida, BigDecimal recargaImporte, Duration recargaPeriodo) {
}
