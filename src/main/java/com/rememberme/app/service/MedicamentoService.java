package com.rememberme.app.service;

import com.rememberme.app.dto.MedicamentoDto;
import com.rememberme.app.entity.Medicamento;

import java.util.List;

public interface MedicamentoService {

    List<Medicamento> listarActivosPorCuidador(Long cuidadorId);

    List<Medicamento> listarActivosPorPaciente(Long pacienteId);

    Medicamento crearMedicamento(MedicamentoDto dto, Long cuidadorId);

    void desactivarMedicamento(Long medicamentoId, Long cuidadorId);
}