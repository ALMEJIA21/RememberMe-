package com.rememberme.app.service;

import com.rememberme.app.dto.AsignacionCuidadorDto;
import com.rememberme.app.entity.CuidadorPaciente;
import com.rememberme.app.entity.Paciente;
import com.rememberme.app.entity.Usuario;

import java.util.List;
import java.util.Map;

public interface AdminService {

    Map<String, Object> obtenerMetricasGlobales();

    List<Usuario> listarDirectorioUsuarios();

    List<Usuario> listarCuidadoresConAlertaDesgaste();

    List<Usuario> listarCuidadores();

    List<Paciente> listarPacientesActivos();

    List<CuidadorPaciente> listarAsignaciones();

    void vincularCuidadorPaciente(AsignacionCuidadorDto dto);

    void cambiarRolPrincipal(Long cuidadorId, Long pacienteId, boolean esPrincipal);

    void revocarVinculo(Long cuidadorId, Long pacienteId);
}