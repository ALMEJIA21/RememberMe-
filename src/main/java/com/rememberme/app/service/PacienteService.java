package com.rememberme.app.service;

import com.rememberme.app.dto.PacienteDto;
import com.rememberme.app.entity.CuidadorPaciente;
import com.rememberme.app.entity.Paciente;

import java.util.List;

public interface PacienteService {

    List<CuidadorPaciente> listarVinculosPorCuidador(Long cuidadorId);

    Paciente obtenerPacienteParaCuidador(Long pacienteId, Long cuidadorId);

    Paciente crearPacienteParaCuidador(PacienteDto dto, Long cuidadorId);

    Paciente actualizarPacienteParaCuidador(Long pacienteId, PacienteDto dto, Long cuidadorId);

    void desactivarPacienteParaCuidador(Long pacienteId, Long cuidadorId);

    Paciente obtenerExpedientePropio(Long usuarioId);

    Paciente actualizarPerfilPropio(Long usuarioId, PacienteDto dto);

    List<CuidadorPaciente> listarCuidadoresAutorizados(Long pacienteId);
}