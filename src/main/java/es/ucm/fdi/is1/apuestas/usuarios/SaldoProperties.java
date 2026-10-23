package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de las moneditas (application.properties, prefijo apuestas.saldo) para el saldo
 * de bienvenida y las recargas gratuitas (HU-14). Spring crea este record con esos valores.
 * Por ejemplo, {@code apuestas.saldo.recarga-importe=50} llega como {@code recargaImporte}.
 *
 * <p>Un {@code record} es una clase de solo datos: Java genera el constructor y un método por campo
 * ({@code saldo.bienvenida()}, sin "get"). Sus valores no se pueden cambiar después de crearlo.
 *
 * @param bienvenida     saldo inicial al registrarse
 * @param recargaImporte importe de cada recarga gratuita
 * @param recargaPeriodo tiempo mínimo entre recargas gratuitas
 */
@ConfigurationProperties("apuestas.saldo")
public record SaldoProperties(BigDecimal bienvenida, BigDecimal recargaImporte, Duration recargaPeriodo) {
}
