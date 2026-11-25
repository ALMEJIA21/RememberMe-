package com.rememberme.app.repository;

import com.rememberme.app.entity.CuidadorPaciente;
import com.rememberme.app.entity.CuidadorPacienteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuidadorPacienteRepository extends JpaRepository<CuidadorPaciente, CuidadorPacienteId> {

    boolean existsByIdCuidadorIdAndIdPacienteId(Long cuidadorId, Long pacienteId);

    Optional<CuidadorPaciente> findByIdCuidadorIdAndIdPacienteId(Long cuidadorId, Long pacienteId);

    @Query("""
            SELECT cp FROM CuidadorPaciente cp
            JOIN FETCH cp.cuidador c
            JOIN FETCH cp.paciente p
            WHERE p.activo = true
            ORDER BY cp.asignadoEn DESC
            """)
    List<CuidadorPaciente> findAllActivosWithDetails();

    @Query("""
            SELECT cp FROM CuidadorPaciente cp
            JOIN FETCH cp.cuidador c
            WHERE cp.paciente.id = :pacienteId
            ORDER BY cp.esPrincipal DESC, c.nombre ASC
            """)
    List<CuidadorPaciente> findByPacienteIdWithCuidador(@Param("pacienteId") Long pacienteId);

    @Query("""
            SELECT cp FROM CuidadorPaciente cp
            JOIN FETCH cp.paciente p
            WHERE cp.cuidador.id = :cuidadorId
              AND p.activo = true
            ORDER BY cp.esPrincipal DESC, p.nombreCompleto ASC
            """)
    List<CuidadorPaciente> findByCuidadorIdWithPaciente(@Param("cuidadorId") Long cuidadorId);
}