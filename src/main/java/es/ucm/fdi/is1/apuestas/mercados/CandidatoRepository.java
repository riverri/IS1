package es.ucm.fdi.is1.apuestas.mercados;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de los candidatos; se buscan por id al cambiar su cuota o al apostar. */
public interface CandidatoRepository extends JpaRepository<Candidato, Long> {
}
