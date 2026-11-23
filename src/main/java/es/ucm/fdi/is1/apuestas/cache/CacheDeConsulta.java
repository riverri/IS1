package es.ucm.fdi.is1.apuestas.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Resultado de una consulta cara guardado en memoria hasta que cambian los datos de los que sale. Lo crea
 * {@link CachesDeConsultas}, que le avisa cuando se guarda, cambia o borra una entidad de {@link #getEntidades()}.
 *
 * <p>Cada aviso sube la <em>generación</em>, al cambiar (durante el flush) y otra vez al terminar la transacción que
 * hizo el cambio. Un resultado solo vale si se calculó en la generación actual: así no se guarda ni se sirve uno leído
 * antes de que el cambio estuviera confirmado. La transacción que ha cambiado algo, o que tiene cambios sin escribir,
 * no usa la caché: vería datos que aún no son suyos. La duración limita lo viejo que puede ser si los datos cambian
 * desde otra instancia de la aplicación.
 *
 * @param <K> qué se consulta (un criterio de orden, por ejemplo)
 * @param <V> el resultado; debe ser inmutable, porque lo comparten todas las peticiones
 */
public final class CacheDeConsulta<K, V> {

    private record Guardado<V>(V valor, long generacion, long guardadoEn) {
    }

    private final List<Class<?>> entidades;
    private final long duracionNs;
    private final BooleanSupplier sesionSinCambios;
    private final AtomicLong generacion = new AtomicLong();
    private final Map<K, Guardado<V>> guardados = new ConcurrentHashMap<>();
    /** Marca, en la transacción actual, que esta ha cambiado datos de la caché. */
    private final Object cambiosEnTransaccion = new Object();

    CacheDeConsulta(List<Class<?>> entidades, Duration duracion, BooleanSupplier sesionSinCambios) {
        this.entidades = List.copyOf(entidades);
        this.duracionNs = duracion.toNanos();
        this.sesionSinCambios = sesionSinCambios;
    }

    /** El resultado guardado si sigue valiendo; si no, lo calcula y lo guarda si nada ha cambiado mientras tanto. */
    public V obtener(K clave, Supplier<V> calcular) {
        if (TransactionSynchronizationManager.hasResource(cambiosEnTransaccion) || !sesionSinCambios.getAsBoolean()) {
            return calcular.get();
        }
        long actual = generacion.get();
        long ahora = System.nanoTime();
        Guardado<V> guardado = guardados.get(clave);
        if (guardado != null && guardado.generacion() == actual && ahora - guardado.guardadoEn() < duracionNs) {
            return guardado.valor();
        }
        V valor = calcular.get();
        // Si algo ha cambiado durante el cálculo, el valor puede ser viejo: se devuelve, pero no se guarda
        if (generacion.get() == actual) {
            guardados.put(clave, new Guardado<>(valor, actual, ahora));
        }
        return valor;
    }

    /** Si los cambios de esta entidad invalidan la caché. */
    boolean dependeDe(Object entidad) {
        return entidades.stream().anyMatch(tipo -> tipo.isInstance(entidad));
    }

    /** Ha cambiado una entidad de la que depende: deja de valer ahora y otra vez al acabar la transacción. */
    void cambio() {
        generacion.incrementAndGet();
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || TransactionSynchronizationManager.hasResource(cambiosEnTransaccion)) {
            return;
        }
        TransactionSynchronizationManager.bindResource(cambiosEnTransaccion, Boolean.TRUE);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                TransactionSynchronizationManager.unbindResourceIfPossible(cambiosEnTransaccion);
                generacion.incrementAndGet();
            }
        });
    }

    /** Olvida lo guardado (pruebas y cambios hechos fuera de JPA). */
    public void invalidar() {
        generacion.incrementAndGet();
        guardados.clear();
    }

    List<Class<?>> getEntidades() {
        return entidades;
    }
}
