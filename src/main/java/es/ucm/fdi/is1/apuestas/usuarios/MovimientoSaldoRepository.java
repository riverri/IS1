package es.ucm.fdi.is1.apuestas.usuarios;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data del libro de movimientos del saldo (HU-55). Las consultas salen del nombre del método. */
public interface MovimientoSaldoRepository extends JpaRepository<MovimientoSaldo, Long> {

    /**
     * Los 100 movimientos más recientes del usuario. Con la misma fecha, el último creado primero, así el de
     * arriba es siempre el que deja el saldo actual.
     */
    List<MovimientoSaldo> findTop100ByUsuarioOrderByFechaDescIdDesc(Usuario usuario);

    /** Todos, del primero al último; lo usan las pruebas para comprobar que el saldo cuadra. */
    List<MovimientoSaldo> findByUsuarioOrderByIdAsc(Usuario usuario);

    /**
     * Lo jugado desde una fecha en movimientos de esos tipos (los cargos son negativos: se suman cambiados de signo).
     * Lo usa el juego responsable para el casino y los demás juegos (HU-10).
     */
    @Query("""
            select coalesce(sum(-m.importe), 0) from MovimientoSaldo m
            where m.usuario = :usuario and m.fecha >= :desde and m.tipo in :tipos and m.importe < 0""")
    BigDecimal jugadoDesde(Usuario usuario, LocalDateTime desde, Collection<TipoMovimiento> tipos);
}
