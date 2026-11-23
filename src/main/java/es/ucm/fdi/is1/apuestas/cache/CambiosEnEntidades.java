package es.ucm.fdi.is1.apuestas.cache;

import org.springframework.beans.factory.ObjectProvider;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

/**
 * Escucha de JPA ({@code @EntityListeners}) que avisa a {@link CachesDeConsultas} cada vez que se inserta, cambia o
 * borra una entidad. Hibernate la crea a través de Spring; {@link ObjectProvider} retrasa buscar las cachés hasta el
 * primer cambio, cuando la base de datos ya está lista.
 */
public class CambiosEnEntidades {

    private final ObjectProvider<CachesDeConsultas> caches;

    public CambiosEnEntidades(ObjectProvider<CachesDeConsultas> caches) {
        this.caches = caches;
    }

    @PostPersist
    @PostUpdate
    @PostRemove
    void alCambiar(Object entidad) {
        CachesDeConsultas disponibles = caches.getIfAvailable();
        if (disponibles != null) {
            disponibles.cambio(entidad);
        }
    }
}
