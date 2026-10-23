package es.ucm.fdi.is1.apuestas.notificaciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio Spring Data de los avisos (HU-37). Las consultas salen del nombre de cada método. */
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Los 50 avisos más recientes del usuario; con la misma fecha, el último creado primero ({@code Top50} = como mucho 50). */
    List<Notificacion> findTop50ByUsuarioOrderByFechaDescIdDesc(Usuario usuario);

    /** Cuántos avisos sin leer tiene el usuario con ese email ({@code UsuarioEmail} = usuario.email). */
    long countByUsuarioEmailAndLeidaFalse(String email);

    /** Avisos del usuario que aún no ha leído. */
    List<Notificacion> findByUsuarioAndLeidaFalse(Usuario usuario);

    /** Borra todos los avisos del usuario (baja de la cuenta, HU-18). */
    void deleteByUsuario(Usuario usuario);
}
