package es.ucm.fdi.is1.apuestas.fantasy;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.LockModeType;

public interface EquipoFantasyRepository extends JpaRepository<EquipoFantasy, Long> {

    Optional<EquipoFantasy> findByUsuario(Usuario usuario);

    /**
     * El equipo, bloqueado hasta el final de la transacción: dos fichajes o ventas a la vez (doble clic, dos pestañas)
     * se hacen uno detrás de otro, y el segundo ya ve al primero. El @Version no basta: añadir un fichaje no cambia
     * ninguna columna del equipo, así que la versión no sube.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EquipoFantasy e where e.usuario = :usuario")
    Optional<EquipoFantasy> paraCambiar(@Param("usuario") Usuario usuario);
}
