package com.rememberme.app.repository;

import com.rememberme.app.entity.RegistroToma;
import com.rememberme.app.entity.enums.EstadoToma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistroTomaRepository extends JpaRepository<RegistroToma, Long> {

    boolean existsByMedicamentoIdAndHoraProgramada(Long medicamentoId, LocalDateTime horaProgramada);

    @Query("""
            SELECT t FROM RegistroToma t
            JOIN FETCH t.medicamento m
            JOIN FETCH m.paciente p
            LEFT JOIN FETCH p.usuario pu
            LEFT JOIN FETCH t.administradoPor u
            WHERE t.id = :id
            """)
    Optional<RegistroToma> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT t FROM RegistroToma t
            JOIN FETCH t.medicamento m
            JOIN FETCH m.paciente p
            LEFT JOIN FETCH t.administradoPor u
            WHERE m.activo = true
              AND p.activo = true
              AND t.horaProgramada BETWEEN :inicio AND :fin
              AND EXISTS (
                  SELECT 1 FROM CuidadorPaciente cp
                  WHERE cp.cuidador.id = :cuidadorId
                    AND cp.paciente.id = p.id
              )
            ORDER BY t.horaProgramada ASC
            """)
    List<RegistroToma> findAgendaByCuidadorAndRango(@Param("cuidadorId") Long cuidadorId,
                                                    @Param("inicio") LocalDateTime inicio,
                                                    @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT t FROM RegistroToma t
            JOIN FETCH t.medicamento m
            JOIN FETCH m.paciente p
            LEFT JOIN FETCH t.administradoPor u
            WHERE m.activo = true
              AND p.activo = true
              AND p.usuario.id = :usuarioId
              AND t.horaProgramada BETWEEN :inicio AND :fin
            ORDER BY t.horaProgramada ASC
            """)
    List<RegistroToma> findAgendaByPacienteUsuarioAndRango(@Param("usuarioId") Long usuarioId,
                                                           @Param("inicio") LocalDateTime inicio,
                                                           @Param("fin") LocalDateTime fin);

    long countByEstadoAndHoraProgramadaBetween(EstadoToma estado, LocalDateTime inicio, LocalDateTime fin);

    long countByHoraProgramadaBetween(LocalDateTime inicio, LocalDateTime fin);
}