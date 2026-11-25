package com.rememberme.app.service;

import com.rememberme.app.dto.BienestarDto;
import com.rememberme.app.entity.BienestarCuidador;

import java.util.List;

public interface BienestarService {

    BienestarCuidador registrarCheckIn(Long cuidadorId, BienestarDto dto);

    List<BienestarCuidador> obtenerHistorialReciente(Long cuidadorId);

    boolean tieneAlertaDesgasteConsecutivo(Long cuidadorId);
}