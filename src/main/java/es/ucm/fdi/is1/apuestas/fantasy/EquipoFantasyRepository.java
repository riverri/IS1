package es.ucm.fdi.is1.apuestas.fantasy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import jakarta.persistence.LockModeType;

public interface EquipoFantasyRepository extends JpaRepository<EquipoFantasy, Long> {

    /** Fila ligera de la clasificación, calculada sin cargar los fichajes ni el historial de eventos. */
    interface FilaClasificacion {
        Long getId();

        String getNombreEquipo();

        String getNombreUsuario();

        String getEmail();

        LocalDateTime getCreado();

        Integer getPuntos();

        Integer getPuesto();
    }

    /**
     * Suma los puntos directamente en la base de datos. La condición temporal mantiene la semántica de fichajes y
     * ventas; las columnas club/posición son las instantáneas guardadas al fichar (CA-82.1).
     */
    @Query(value = """
            with puntuados as (
            select ef.id as id, ef.nombre as nombreEquipo, u.nombre as nombreUsuario, u.email as email,
                   ef.creado as creado,
                   cast(coalesce(sum(case when ev.id is null then 0 else
                       case when ev.resultado = 'EMPATE' then 1
                            when ev.resultado = ev.gana then 3
                            else 0 end
                       + case when ev.a_favor is null or ev.en_contra is null then 0 else
                           case when ev.en_contra = 0
                                then case f.posicion when 'PORTERO' then 4 when 'DEFENSA' then 3
                                                    when 'CENTROCAMPISTA' then 1 else 0 end
                                else 0 end
                           + ev.a_favor * case f.posicion when 'DELANTERO' then 2 when 'CENTROCAMPISTA' then 1 else 0 end
                           + case when ev.en_contra >= 3 and f.posicion in ('PORTERO', 'DEFENSA') then -1 else 0 end
                         end
                   end), 0) as integer) as puntos
            from equipo_fantasy ef
            join usuario u on u.id = ef.usuario_id
            left join fichaje_fantasy f on f.equipo_id = ef.id
            left join (
                select id, local_id as club_id, fecha_hora, resultado, 'LOCAL' as gana, goles_local as a_favor, goles_visitante as en_contra
                from evento where estado = 'FINALIZADO'
                union all
                select id, visitante_id, fecha_hora, resultado, 'VISITANTE', goles_visitante, goles_local
                from evento where estado = 'FINALIZADO'
            ) ev on ev.club_id = f.club_id and ev.fecha_hora >= f.desde and (f.hasta is null or ev.fecha_hora < f.hasta)
            where u.eliminado = false
            group by ef.id, ef.nombre, u.nombre, u.email, ef.creado
            ), posiciones as (
                select p.*, cast(row_number() over (order by p.puntos desc, p.creado asc, p.id asc) as integer) as puesto
                from puntuados p
            )
            select p.id, p.nombreEquipo, p.nombreUsuario, p.email, p.creado, p.puntos, p.puesto
            from posiciones p where p.puesto <= 20 or p.email = :emailActual order by p.puesto
            """, nativeQuery = true)
    List<FilaClasificacion> clasificacionAgregada(@Param("emailActual") String emailActual);

    /** Equipo Fantasy para su página, que muestra plantilla, fichajes y puntos; el ranking usa una agregación aparte. */
    @EntityGraph(attributePaths = {"fichajes", "fichajes.jugador", "fichajes.club"})
    Optional<EquipoFantasy> findByUsuario(Usuario usuario);

    /**
     * El equipo, bloqueado hasta el final de la transacción: dos fichajes o ventas a la vez (doble clic, dos pestañas)
     * se hacen uno detrás de otro, y el segundo ya ve al primero. El @Version no basta: añadir un fichaje no cambia
     * ninguna columna del equipo, así que la versión no sube.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EquipoFantasy e where e.usuario = :usuario")
    Optional<EquipoFantasy> paraCambiar(@Param("usuario") Usuario usuario);

    /** Inicializa el historial solo después de bloquear la fila del equipo, sin ampliar el alcance del bloqueo. */
    @EntityGraph(attributePaths = {"fichajes", "fichajes.jugador", "fichajes.club"})
    @Query("select e from EquipoFantasy e where e.id = :id")
    Optional<EquipoFantasy> conFichajes(@Param("id") Long id);
}
