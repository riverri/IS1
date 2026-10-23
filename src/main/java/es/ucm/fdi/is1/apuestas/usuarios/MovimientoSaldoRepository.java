package es.ucm.fdi.is1.apuestas.usuarios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data del libro de movimientos del saldo (HU-55). Las consultas salen del nombre del método. */
public interface MovimientoSaldoRepository extends JpaRepository<MovimientoSaldo, Long> {

    /**
     * Los 100 movimientos más recientes del usuario. Con la misma fecha, el último creado primero, así el de
     * arriba es siempre el que deja el saldo actual.
     */
    List<MovimientoSaldo> findTop100ByUsuarioOrderByFechaDescIdDesc(Usuario usuario);

    /** Todos, del primero al último; lo usan las pruebas para comprobar que el saldo cuadra. */
    List<MovimientoSaldo> findByUsuarioOrderByIdAsc(Usuario usuario);
}
