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
     * Los últimos 100, del más reciente al más antiguo. Por id y no por fecha: la fecha es la hora de Madrid y, al
     * atrasar el reloj en octubre, un movimiento posterior puede tener una hora anterior (CA-55.2).
     */
    List<MovimientoSaldo> findTop100ByUsuarioOrderByIdDesc(Usuario usuario);

    /** Todos, del primero al último; lo usan las pruebas para comprobar que el saldo cuadra. */
    List<MovimientoSaldo> findByUsuarioOrderByIdAsc(Usuario usuario);

    /** Los de un usuario desde una fecha: resumen semanal (HU-92). */
    List<MovimientoSaldo> findByUsuarioAndFechaGreaterThanEqual(Usuario usuario, LocalDateTime desde);

    /** Movimientos de todos los usuarios desde una fecha, de esos tipos: beneficio diario de la casa (HU-95). */
    List<MovimientoSaldo> findByFechaGreaterThanEqualAndTipoIn(LocalDateTime desde, Collection<TipoMovimiento> tipos);

    /**
     * Lo jugado desde una fecha en movimientos de esos tipos (los cargos son negativos: se suman cambiados de signo).
     * Lo usa el juego responsable para el casino y los demás juegos (HU-10).
     */
    @Query("""
            select coalesce(sum(-m.importe), 0) from MovimientoSaldo m
            where m.usuario = :usuario and m.fecha >= :desde and m.tipo in :tipos and m.importe < 0""")
    BigDecimal jugadoDesde(Usuario usuario, LocalDateTime desde, Collection<TipoMovimiento> tipos);
}
