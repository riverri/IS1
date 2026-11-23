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

    /** Equipo de la clasificación, sin sus puntos (los da {@link #puntosPorEquipo}) ni sus fichajes. */
    interface FilaClasificacion {
        Long getId();

        String getNombreEquipo();

        String getNombreUsuario();

        String getEmail();

        LocalDateTime getCreado();
    }

    /** Puntos de un equipo, de {@link #puntosPorEquipo}. */
    interface PuntosEquipo {
        Long getId();

        Integer getPuntos();
    }

    /** Equipos de las cuentas que siguen activas, para la clasificación (HU-83): una fila ligera por equipo. */
    @Query("""
            select e.id as id, e.nombre as nombreEquipo, u.nombre as nombreUsuario, u.email as email,
                   e.creado as creado
            from EquipoFantasy e join e.usuario u where u.eliminado = false""")
    List<FilaClasificacion> filasClasificacion();

    /**
     * Puntos de cada equipo, sumados en la base de datos. La condición temporal mantiene la semántica de fichajes y
     * ventas; las columnas club/posición son las instantáneas guardadas al fichar (CA-82.1). Es la consulta cara de la
     * clasificación: {@link FantasyService} la guarda hasta que cambia un partido, un fichaje o un equipo.
     */
    @Query(value = """
            select ef.id as id,
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
            left join fichaje_fantasy f on f.equipo_id = ef.id
            left join (
                select id, local_id as club_id, fecha_hora, resultado, 'LOCAL' as gana, goles_local as a_favor, goles_visitante as en_contra
                from evento where estado = 'FINALIZADO'
                union all
                select id, visitante_id, fecha_hora, resultado, 'VISITANTE', goles_visitante, goles_local
                from evento where estado = 'FINALIZADO'
            ) ev on ev.club_id = f.club_id and ev.fecha_hora >= f.desde and (f.hasta is null or ev.fecha_hora < f.hasta)
            group by ef.id
            """, nativeQuery = true)
    List<PuntosEquipo> puntosPorEquipo();

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
