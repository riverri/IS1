package es.ucm.fdi.is1.apuestas.quiniela;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;

/** Repositorio de {@link BoletoQuiniela}. */
public interface BoletoQuinielaRepository extends JpaRepository<BoletoQuiniela, Long> {

    List<BoletoQuiniela> findByQuinielaOrderByIdAsc(Quiniela quiniela);

    Optional<BoletoQuiniela> findByQuinielaAndUsuario(Quiniela quiniela, Usuario usuario);

    long countByQuiniela(Quiniela quiniela);

    /** Todos los boletos de un usuario: logros (HU-88). */
    List<BoletoQuiniela> findByUsuario(Usuario usuario);

    /** Boletos de quinielas ya repartidas, con su usuario: estadísticas del ranking por tipo de juego (HU-84). */
    @Query("""
            select b from BoletoQuiniela b join fetch b.usuario
            where b.quiniela.estado = es.ucm.fdi.is1.apuestas.quiniela.EstadoQuiniela.REPARTIDA""")
    List<BoletoQuiniela> deQuinielasRepartidas();
}
