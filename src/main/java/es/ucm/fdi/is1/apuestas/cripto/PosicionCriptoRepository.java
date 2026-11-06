package es.ucm.fdi.is1.apuestas.cripto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio Spring Data de lo que tiene cada usuario de cada criptomoneda (HU-72). */
public interface PosicionCriptoRepository extends JpaRepository<PosicionCripto, Long> {

    Optional<PosicionCripto> findByUsuarioAndCripto(Usuario usuario, Criptomoneda cripto);

    List<PosicionCripto> findByUsuario(Usuario usuario);
}
