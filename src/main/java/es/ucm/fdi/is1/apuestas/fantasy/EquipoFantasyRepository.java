package es.ucm.fdi.is1.apuestas.fantasy;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

public interface EquipoFantasyRepository extends JpaRepository<EquipoFantasy, Long> {

    Optional<EquipoFantasy> findByUsuario(Usuario usuario);
}
