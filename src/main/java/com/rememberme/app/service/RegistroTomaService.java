package com.rememberme.app.service;

import com.rememberme.app.dto.TomaVerificacionDto;
import com.rememberme.app.entity.RegistroToma;
import com.rememberme.app.security.CustomUserDetails;

import java.util.List;

public interface RegistroTomaService {

    List<RegistroToma> obtenerAgendaHoyCuidador(Long cuidadorId);

    List<RegistroToma> obtenerAgendaHoyPaciente(Long usuarioId);

    int calcularPorcentajeAdherencia(List<RegistroToma> tomas);

    RegistroToma verificarYRegistrarToma(Long tomaId, TomaVerificacionDto dto, CustomUserDetails usuarioActual);
}