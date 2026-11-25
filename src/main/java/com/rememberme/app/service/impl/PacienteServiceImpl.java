package com.rememberme.app.service.impl;

import com.rememberme.app.dto.PacienteDto;
import com.rememberme.app.entity.*;
import com.rememberme.app.exception.ForbiddenException;
import com.rememberme.app.exception.ResourceNotFoundException;
import com.rememberme.app.repository.CuidadorPacienteRepository;
import com.rememberme.app.repository.MedicamentoRepository;
import com.rememberme.app.repository.PacienteRepository;
import com.rememberme.app.repository.UsuarioRepository;
import com.rememberme.app.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final CuidadorPacienteRepository cuidadorPacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicamentoRepository medicamentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CuidadorPaciente> listarVinculosPorCuidador(Long cuidadorId) {
        return cuidadorPacienteRepository.findByCuidadorIdWithPaciente(cuidadorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Paciente obtenerPacienteParaCuidador(Long pacienteId, Long cuidadorId) {
        Paciente paciente = pacienteRepository.findByIdAndActivoTrue(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado o inactivo."));

        boolean vinculado = cuidadorPacienteRepository.existsByIdCuidadorIdAndIdPacienteId(cuidadorId, pacienteId);
        if (!vinculado) {
            throw new ForbiddenException("No tienes autorización para acceder al expediente de este paciente.");
        }
        return paciente;
    }

    @Override
    @Transactional
    public Paciente crearPacienteParaCuidador(PacienteDto dto, Long cuidadorId) {
        Usuario cuidador = usuarioRepository.findById(cuidadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador no encontrado."));

        Paciente paciente = pacienteRepository.save(Paciente.builder()
                .nombreCompleto(dto.getNombreCompleto().trim())
                .fechaNacimiento(dto.getFechaNacimiento())
                .alergias(dto.getAlergias())
                .notasClinicas(dto.getNotasClinicas())
                .activo(true)
                .build());

        cuidadorPacienteRepository.save(CuidadorPaciente.builder()
                .id(new CuidadorPacienteId(cuidador.getId(), paciente.getId()))
                .cuidador(cuidador)
                .paciente(paciente)
                .esPrincipal(true)
                .build());

        return paciente;
    }

    @Override
    @Transactional
    public Paciente actualizarPacienteParaCuidador(Long pacienteId, PacienteDto dto, Long cuidadorId) {
        Paciente paciente = obtenerPacienteParaCuidador(pacienteId, cuidadorId);
        paciente.setNombreCompleto(dto.getNombreCompleto().trim());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setAlergias(dto.getAlergias());
        paciente.setNotasClinicas(dto.getNotasClinicas());
        return pacienteRepository.save(paciente);
    }

    @Override
    @Transactional
    public void desactivarPacienteParaCuidador(Long pacienteId, Long cuidadorId) {
        Paciente paciente = obtenerPacienteParaCuidador(pacienteId, cuidadorId);
        // Borrado lógico estricto: nunca se elimina físicamente la fila
        paciente.setActivo(false);
        pacienteRepository.save(paciente);

        List<Medicamento> medicamentos = medicamentoRepository.findActivosByPacienteId(pacienteId);
        medicamentos.forEach(m -> m.setActivo(false));
        medicamentoRepository.saveAll(medicamentos);
    }

    @Override
    @Transactional(readOnly = true)
    public Paciente obtenerExpedientePropio(Long usuarioId) {
        return pacienteRepository.findByUsuarioIdAndActivoTrue(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró un expediente activo vinculado a tu cuenta."));
    }

    @Override
    @Transactional
    public Paciente actualizarPerfilPropio(Long usuarioId, PacienteDto dto) {
        Paciente paciente = obtenerExpedientePropio(usuarioId);
        if (dto.getNombreCompleto() != null && !dto.getNombreCompleto().isBlank()) {
            paciente.setNombreCompleto(dto.getNombreCompleto().trim());
        }
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setAlergias(dto.getAlergias());
        paciente.setNotasClinicas(dto.getNotasClinicas());
        return pacienteRepository.save(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuidadorPaciente> listarCuidadoresAutorizados(Long pacienteId) {
        return cuidadorPacienteRepository.findByPacienteIdWithCuidador(pacienteId);
    }
}