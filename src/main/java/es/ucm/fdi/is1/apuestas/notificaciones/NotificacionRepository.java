package es.ucm.fdi.is1.apuestas.notificaciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findTop50ByUsuarioOrderByFechaDescIdDesc(Usuario usuario);

    long countByUsuarioEmailAndLeidaFalse(String email);

    List<Notificacion> findByUsuarioAndLeidaFalse(Usuario usuario);
}
