package com.rememberme.app.service.impl;

import com.rememberme.app.dto.MedicamentoDto;
import com.rememberme.app.entity.Medicamento;
import com.rememberme.app.entity.Paciente;
import com.rememberme.app.entity.RegistroToma;
import com.rememberme.app.entity.enums.EstadoToma;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.exception.ForbiddenException;
import com.rememberme.app.exception.ResourceNotFoundException;
import com.rememberme.app.repository.CuidadorPacienteRepository;
import com.rememberme.app.repository.MedicamentoRepository;
import com.rememberme.app.repository.RegistroTomaRepository;
import com.rememberme.app.service.MedicamentoService;
import com.rememberme.app.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentoServiceImpl implements MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final RegistroTomaRepository registroTomaRepository;
    private final CuidadorPacienteRepository cuidadorPacienteRepository;
    private final PacienteService pacienteService;

    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarActivosPorCuidador(Long cuidadorId) {
        return medicamentoRepository.findActivosByCuidadorId(cuidadorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarActivosPorPaciente(Long pacienteId) {
        return medicamentoRepository.findActivosByPacienteId(pacienteId);
    }

    @Override
    @Transactional
    public Medicamento crearMedicamento(MedicamentoDto dto, Long cuidadorId) {
        Paciente paciente = pacienteService.obtenerPacienteParaCuidador(dto.getPacienteId(), cuidadorId);

        if (dto.getCantidadDosis() == null || dto.getCantidadDosis().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("La cantidad de dosis debe ser mayor a 0.");
        }
        if (dto.getFrecuenciaHoras() == null || dto.getFrecuenciaHoras() <= 0) {
            throw new BusinessRuleException("La frecuencia en horas debe ser mayor a 0.");
        }
        if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(dto.getFechaHoraInicio().toLocalDate())) {
            throw new BusinessRuleException("La fecha de fin no puede ser anterior al inicio del tratamiento.");
        }

        Medicamento medicamento = medicamentoRepository.save(Medicamento.builder()
                .paciente(paciente)
                .nombre(dto.getNombre().trim())
                .principioActivo(dto.getPrincipioActivo() != null ? dto.getPrincipioActivo().trim() : null)
                .cantidadDosis(dto.getCantidadDosis())
                .unidadDosis(dto.getUnidadDosis().trim())
                .viaAdministracion(dto.getViaAdministracion())
                .frecuenciaHoras(dto.getFrecuenciaHoras())
                .esAltoRiesgo(dto.isEsAltoRiesgo())
                .instrucciones(dto.getInstrucciones())
                .fechaHoraInicio(dto.getFechaHoraInicio())
                .fechaFin(dto.getFechaFin())
                .activo(true)
                .build());

        generarTomasVentanaInmediata(medicamento);
        return medicamento;
    }

    @Override
    @Transactional
    public void desactivarMedicamento(Long medicamentoId, Long cuidadorId) {
        Medicamento medicamento = medicamentoRepository.findByIdActivaWithPaciente(medicamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado o ya inactivo."));

        boolean vinculado = cuidadorPacienteRepository.existsByIdCuidadorIdAndIdPacienteId(
                cuidadorId, medicamento.getPaciente().getId());
        if (!vinculado) {
            throw new ForbiddenException("No tienes permiso para modificar medicamentos de este paciente.");
        }

        medicamento.setActivo(false);
        medicamentoRepository.save(medicamento);
    }

    private void generarTomasVentanaInmediata(Medicamento medicamento) {
        LocalDateTime inicioVentana = LocalDate.now().atStartOfDay();
        LocalDateTime finVentana = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);
        if (medicamento.getFechaFin() != null) {
            LocalDateTime limiteFin = medicamento.getFechaFin().atTime(LocalTime.MAX);
            if (limiteFin.isBefore(finVentana)) {
                finVentana = limiteFin;
            }
        }

        LocalDateTime cursor = medicamento.getFechaHoraInicio();
        while (cursor.isBefore(inicioVentana)) {
            cursor = cursor.plusHours(medicamento.getFrecuenciaHoras());
        }

        List<RegistroToma> nuevasTomas = new ArrayList<>();
        int maxIteraciones = 48;
        int iteracion = 0;

        while (!cursor.isAfter(finVentana) && iteracion < maxIteraciones) {
            if (!registroTomaRepository.existsByMedicamentoIdAndHoraProgramada(medicamento.getId(), cursor)) {
                nuevasTomas.add(RegistroToma.builder()
                        .medicamento(medicamento)
                        .horaProgramada(cursor)
                        .estado(EstadoToma.PENDIENTE)
                        .build());
            }
            cursor = cursor.plusHours(medicamento.getFrecuenciaHoras());
            iteracion++;
        }

        if (!nuevasTomas.isEmpty()) {
            registroTomaRepository.saveAll(nuevasTomas);
        }
    }
}