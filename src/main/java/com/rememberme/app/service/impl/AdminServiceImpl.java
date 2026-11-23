package com.rememberme.app.service.impl;

import com.rememberme.app.dto.AsignacionCuidadorDto;
import com.rememberme.app.entity.*;
import com.rememberme.app.entity.enums.EstadoToma;
import com.rememberme.app.entity.enums.RolUsuario;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.exception.ResourceNotFoundException;
import com.rememberme.app.repository.*;
import com.rememberme.app.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final RegistroTomaRepository registroTomaRepository;
    private final CuidadorPacienteRepository cuidadorPacienteRepository;
    private final BienestarCuidadorRepository bienestarCuidadorRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerMetricasGlobales() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);

        long totalTomasHoy = registroTomaRepository.countByHoraProgramadaBetween(inicio, fin);
        long administradasHoy = registroTomaRepository.countByEstadoAndHoraProgramadaBetween(
                EstadoToma.ADMINISTRADO, inicio, fin);
        int adherenciaGlobal = totalTomasHoy == 0 ? 0 : (int) Math.round((administradasHoy * 100.0) / totalTomasHoy);

        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("totalUsuarios", usuarioRepository.count());
        metricas.put("totalCuidadores", usuarioRepository.countByRol(RolUsuario.ROL_CUIDADOR));
        metricas.put("totalPacientesActivos", pacienteRepository.countByActivoTrue());
        metricas.put("totalMedicamentosActivos", medicamentoRepository.countByActivoTrue());
        metricas.put("totalAltoRiesgo", medicamentoRepository.countByActivoTrueAndEsAltoRiesgoTrue());
        metricas.put("totalTomasHoy", totalTomasHoy);
        metricas.put("administradasHoy", administradasHoy);
        metricas.put("adherenciaGlobal", adherenciaGlobal);
        return metricas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarDirectorioUsuarios() {
        return usuarioRepository.findAllByOrderByCreadoEnDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarCuidadoresConAlertaDesgaste() {
        LocalDate desde = LocalDate.now().minusDays(6);
        List<BienestarCuidador> registros = bienestarCuidadorRepository.findRecientesConUsuario(desde);

        Map<Long, List<BienestarCuidador>> porCuidador = registros.stream()
                .collect(Collectors.groupingBy(b -> b.getUsuario().getId(), LinkedHashMap::new, Collectors.toList()));

        List<Usuario> enAlerta = new ArrayList<>();
        for (List<BienestarCuidador> lista : porCuidador.values()) {
            if (BienestarServiceImpl.detectarDosDiasConsecutivosEnDesgaste(lista)) {
                enAlerta.add(lista.get(0).getUsuario());
            }
        }
        return enAlerta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarCuidadores() {
        return usuarioRepository.findByRolOrderByNombreAsc(RolUsuario.ROL_CUIDADOR);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paciente> listarPacientesActivos() {
        return pacienteRepository.findByActivoTrueOrderByNombreCompletoAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuidadorPaciente> listarAsignaciones() {
        return cuidadorPacienteRepository.findAllActivosWithDetails();
    }

    @Override
    @Transactional
    public void vincularCuidadorPaciente(AsignacionCuidadorDto dto) {
        Usuario cuidador = usuarioRepository.findById(dto.getCuidadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador no encontrado."));
        if (cuidador.getRol() != RolUsuario.ROL_CUIDADOR) {
            throw new BusinessRuleException("Solo se pueden vincular usuarios con rol Cuidador.");
        }

        Paciente paciente = pacienteRepository.findByIdAndActivoTrue(dto.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado o inactivo."));

        CuidadorPacienteId id = new CuidadorPacienteId(cuidador.getId(), paciente.getId());
        CuidadorPaciente vinculo = cuidadorPacienteRepository.findById(id)
                .orElse(CuidadorPaciente.builder()
                        .id(id)
                        .cuidador(cuidador)
                        .paciente(paciente)
                        .build());

        vinculo.setEsPrincipal(dto.isEsPrincipal());
        cuidadorPacienteRepository.save(vinculo);
    }

    @Override
    @Transactional
    public void cambiarRolPrincipal(Long cuidadorId, Long pacienteId, boolean esPrincipal) {
        CuidadorPaciente vinculo = cuidadorPacienteRepository
                .findByIdCuidadorIdAndIdPacienteId(cuidadorId, pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada."));
        vinculo.setEsPrincipal(esPrincipal);
        cuidadorPacienteRepository.save(vinculo);
    }

    @Override
    @Transactional
    public void revocarVinculo(Long cuidadorId, Long pacienteId) {
        CuidadorPacienteId id = new CuidadorPacienteId(cuidadorId, pacienteId);
        if (!cuidadorPacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("La asignación especificada no existe.");
        }
        cuidadorPacienteRepository.deleteById(id);
    }
}