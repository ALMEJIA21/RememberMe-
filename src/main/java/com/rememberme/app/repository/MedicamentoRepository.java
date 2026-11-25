package com.rememberme.app.repository;

import com.rememberme.app.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    @Query("""
            SELECT m FROM Medicamento m
            JOIN FETCH m.paciente p
            WHERE p.id = :pacienteId
              AND m.activo = true
              AND p.activo = true
            ORDER BY m.esAltoRiesgo DESC, m.nombre ASC
            """)
    List<Medicamento> findActivosByPacienteId(@Param("pacienteId") Long pacienteId);

    @Query("""
            SELECT m FROM Medicamento m
            JOIN FETCH m.paciente p
            WHERE m.id = :id
              AND m.activo = true
              AND p.activo = true
            """)
    Optional<Medicamento> findByIdActivaWithPaciente(@Param("id") Long id);

    @Query("""
            SELECT m FROM Medicamento m
            JOIN FETCH m.paciente p
            WHERE m.activo = true
              AND p.activo = true
              AND EXISTS (
                  SELECT 1 FROM CuidadorPaciente cp
                  WHERE cp.cuidador.id = :cuidadorId
                    AND cp.paciente.id = p.id
              )
            ORDER BY p.nombreCompleto ASC, m.esAltoRiesgo DESC, m.nombre ASC
            """)
    List<Medicamento> findActivosByCuidadorId(@Param("cuidadorId") Long cuidadorId);

    long countByActivoTrue();

    long countByActivoTrueAndEsAltoRiesgoTrue();
}