package com.rememberme.app.service.impl;

import com.rememberme.app.dto.TomaVerificacionDto;
import com.rememberme.app.entity.Paciente;
import com.rememberme.app.entity.RegistroToma;
import com.rememberme.app.entity.Usuario;
import com.rememberme.app.entity.enums.EstadoToma;
import com.rememberme.app.entity.enums.RolUsuario;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.exception.ForbiddenException;
import com.rememberme.app.exception.ResourceNotFoundException;
import com.rememberme.app.repository.CuidadorPacienteRepository;
import com.rememberme.app.repository.RegistroTomaRepository;
import com.rememberme.app.repository.UsuarioRepository;
import com.rememberme.app.security.CustomUserDetails;
import com.rememberme.app.service.RegistroTomaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistroTomaServiceImpl implements RegistroTomaService {

    private final RegistroTomaRepository registroTomaRepository;
    private final CuidadorPacienteRepository cuidadorPacienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RegistroToma> obtenerAgendaHoyCuidador(Long cuidadorId) {
        LocalDate hoy = LocalDate.now();
        return registroTomaRepository.findAgendaByCuidadorAndRango(
                cuidadorId, hoy.atStartOfDay(), hoy.atTime(LocalTime.MAX));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroToma> obtenerAgendaHoyPaciente(Long usuarioId) {
        LocalDate hoy = LocalDate.now();
        return registroTomaRepository.findAgendaByPacienteUsuarioAndRango(
                usuarioId, hoy.atStartOfDay(), hoy.atTime(LocalTime.MAX));
    }

    @Override
    public int calcularPorcentajeAdherencia(List<RegistroToma> tomas) {
        if (tomas == null || tomas.isEmpty()) {
            return 0;
        }
        long administradas = tomas.stream()
                .filter(t -> t.getEstado() == EstadoToma.ADMINISTRADO)
                .count();
        return (int) Math.round((administradas * 100.0) / tomas.size());
    }

    @Override
    @Transactional
    public RegistroToma verificarYRegistrarToma(Long tomaId, TomaVerificacionDto dto, CustomUserDetails usuarioActual) {
        RegistroToma toma = registroTomaRepository.findByIdWithDetails(tomaId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de toma no encontrado."));

        validarAccesoMultiTenant(toma.getMedicamento().getPaciente(), usuarioActual);

        Usuario ejecutor = usuarioRepository.findById(usuarioActual.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));

        EstadoToma destino = dto.getEstadoDestino();
        if (destino == null || destino == EstadoToma.PENDIENTE) {
            throw new BusinessRuleException("Debes indicar un estado válido (ADMINISTRADO, OMITIDO o RETRASADO).");
        }

        if (destino == EstadoToma.ADMINISTRADO) {
            // Regla clínica crítica: Protocolo de los 5 Correctos obligatorio
            boolean cincoCorrectos = dto.isVerificacionPaciente()
                    && dto.isVerificacionMedicamento()
                    && dto.isVerificacionDosis()
                    && dto.isVerificacionVia()
                    && dto.isVerificacionHora();

            if (!cincoCorrectos) {
                throw new BusinessRuleException(
                        "Protocolo de los 5 Correctos incompleto: debes confirmar Paciente, Medicamento, Dosis, Vía y Hora antes de marcar como ADMINISTRADO.");
            }

            toma.setVerificacionPaciente(true);
            toma.setVerificacionMedicamento(true);
            toma.setVerificacionDosis(true);
            toma.setVerificacionVia(true);
            toma.setVerificacionHora(true);
            toma.setEstado(EstadoToma.ADMINISTRADO);
            toma.setHoraReal(LocalDateTime.now());
            toma.setMotivoOmision(null);
            toma.setAdministradoPor(ejecutor);

        } else if (destino == EstadoToma.OMITIDO) {
            // Regla clínica crítica: motivo de omisión obligatorio
            if (dto.getMotivoOmision() == null || dto.getMotivoOmision().isBlank()) {
                throw new BusinessRuleException("El motivo de omisión es obligatorio cuando se omite una dosis.");
            }

            toma.setEstado(EstadoToma.OMITIDO);
            toma.setMotivoOmision(dto.getMotivoOmision().trim());
            toma.setHoraReal(LocalDateTime.now());
            toma.setAdministradoPor(ejecutor);
            toma.setVerificacionPaciente(false);
            toma.setVerificacionMedicamento(false);
            toma.setVerificacionDosis(false);
            toma.setVerificacionVia(false);
            toma.setVerificacionHora(false);

        } else if (destino == EstadoToma.RETRASADO) {
            toma.setEstado(EstadoToma.RETRASADO);
        }

        if (dto.getNotas() != null && !dto.getNotas().isBlank()) {
            toma.setNotas(dto.getNotas().trim());
        }

        return registroTomaRepository.save(toma);
    }

    private void validarAccesoMultiTenant(Paciente paciente, CustomUserDetails usuarioActual) {
        if (usuarioActual.getRol() == RolUsuario.ROL_CUIDADOR) {
            boolean vinculado = cuidadorPacienteRepository.existsByIdCuidadorIdAndIdPacienteId(
                    usuarioActual.getId(), paciente.getId());
            if (!vinculado) {
                throw new ForbiddenException("No estás autorizado para registrar tomas de este paciente.");
            }
        } else if (usuarioActual.getRol() == RolUsuario.ROL_PACIENTE) {
            if (paciente.getUsuario() == null || !paciente.getUsuario().getId().equals(usuarioActual.getId())) {
                throw new ForbiddenException("Solo puedes confirmar tomas de tu propio plan terapéutico.");
            }
        } else {
            throw new ForbiddenException("Tu rol no tiene permisos para registrar tomas clínicas.");
        }
    }
}