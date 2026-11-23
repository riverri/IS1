package es.ucm.fdi.is1.apuestas.cache;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Las cachés de consultas de la aplicación (ver {@link CacheDeConsulta}). {@link CambiosEnEntidades} le avisa de
 * cada entidad guardada, cambiada o borrada, y ella se lo pasa a las cachés que dependen de esa entidad.
 */
@Component
public class CachesDeConsultas {

    private final EntityManagerFactory emf;
    private final List<CacheDeConsulta<?, ?>> caches = new CopyOnWriteArrayList<>();

    public CachesDeConsultas(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Una caché nueva que deja de valer cuando cambia alguna entidad de esos tipos. Las entidades tienen que llevar
     * {@code @EntityListeners(CambiosEnEntidades.class)}; si no, sus cambios no se notan hasta que pasa la duración.
     */
    public <K, V> CacheDeConsulta<K, V> crear(Duration duracion, Class<?>... entidades) {
        CacheDeConsulta<K, V> cache = new CacheDeConsulta<>(List.of(entidades), duracion, this::sesionSinCambios);
        caches.add(cache);
        return cache;
    }

    void cambio(Object entidad) {
        for (CacheDeConsulta<?, ?> cache : caches) {
            if (cache.dependeDe(entidad)) {
                cache.cambio();
            }
        }
    }

    /** Fuera de una transacción no hay cambios pendientes; dentro, los que aún no se han escrito (sin flush). */
    private boolean sesionSinCambios() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return true;
        }
        EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
        return em == null || !em.unwrap(Session.class).isDirty();
    }
}
