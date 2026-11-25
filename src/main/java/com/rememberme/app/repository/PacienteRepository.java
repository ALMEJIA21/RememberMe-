package com.rememberme.app.repository;

import com.rememberme.app.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByUsuarioIdAndActivoTrue(Long usuarioId);

    Optional<Paciente> findByIdAndActivoTrue(Long id);

    List<Paciente> findByActivoTrueOrderByNombreCompletoAsc();

    long countByActivoTrue();

    // Multi-tenancy: solo pacientes activos vinculados al cuidador autenticado
    @Query("""
            SELECT cp.paciente FROM CuidadorPaciente cp
            WHERE cp.cuidador.id = :cuidadorId
              AND cp.paciente.activo = true
            ORDER BY cp.esPrincipal DESC, cp.paciente.nombreCompleto ASC
            """)
    List<Paciente> findActivosByCuidadorId(@Param("cuidadorId") Long cuidadorId);

    // Multi-tenancy: acceso individual validando vínculo en cuidador_paciente
    @Query("""
            SELECT cp.paciente FROM CuidadorPaciente cp
            WHERE cp.paciente.id = :pacienteId
              AND cp.cuidador.id = :cuidadorId
              AND cp.paciente.activo = true
            """)
    Optional<Paciente> findByIdAndCuidadorId(@Param("pacienteId") Long pacienteId,
                                             @Param("cuidadorId") Long cuidadorId);
}