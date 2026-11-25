package com.rememberme.app.service.impl;

import com.rememberme.app.dto.BienestarDto;
import com.rememberme.app.entity.BienestarCuidador;
import com.rememberme.app.entity.Usuario;
import com.rememberme.app.exception.ResourceNotFoundException;
import com.rememberme.app.repository.BienestarCuidadorRepository;
import com.rememberme.app.repository.UsuarioRepository;
import com.rememberme.app.service.BienestarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BienestarServiceImpl implements BienestarService {

    private final BienestarCuidadorRepository bienestarCuidadorRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public BienestarCuidador registrarCheckIn(Long cuidadorId, BienestarDto dto) {
        Usuario cuidador = usuarioRepository.findById(cuidadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador no encontrado."));

        LocalDate fecha = dto.getFechaRegistro() != null ? dto.getFechaRegistro() : LocalDate.now();

        BienestarCuidador registro = bienestarCuidadorRepository
                .findByUsuarioIdAndFechaRegistro(cuidadorId, fecha)
                .orElseGet(() -> BienestarCuidador.builder()
                        .usuario(cuidador)
                        .fechaRegistro(fecha)
                        .build());

        registro.setNivelEstres(dto.getNivelEstres());
        registro.setNivelFatiga(dto.getNivelFatiga());
        registro.setHorasSueno(dto.getHorasSueno());
        registro.setEstadoEmocional(dto.getEstadoEmocional() != null ? dto.getEstadoEmocional().trim() : null);
        registro.setNota(dto.getNota() != null ? dto.getNota().trim() : null);

        return bienestarCuidadorRepository.save(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienestarCuidador> obtenerHistorialReciente(Long cuidadorId) {
        return bienestarCuidadorRepository.findTop14ByUsuarioIdOrderByFechaRegistroDesc(cuidadorId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneAlertaDesgasteConsecutivo(Long cuidadorId) {
        LocalDate desde = LocalDate.now().minusDays(6);
        List<BienestarCuidador> recientes = bienestarCuidadorRepository
                .findByUsuarioIdAndFechaRegistroGreaterThanEqualOrderByFechaRegistroDesc(cuidadorId, desde);
        return detectarDosDiasConsecutivosEnDesgaste(recientes);
    }

    public static boolean detectarDosDiasConsecutivosEnDesgaste(List<BienestarCuidador> ordenadosDesc) {
        if (ordenadosDesc == null || ordenadosDesc.size() < 2) {
            return false;
        }
        LocalDate limiteVigencia = LocalDate.now().minusDays(1);
        for (int i = 0; i < ordenadosDesc.size() - 1; i++) {
            BienestarCuidador actual = ordenadosDesc.get(i);
            BienestarCuidador anterior = ordenadosDesc.get(i + 1);

            if (actual.getFechaRegistro().isBefore(limiteVigencia)) {
                break;
            }
            boolean sonConsecutivos = actual.getFechaRegistro().minusDays(1).equals(anterior.getFechaRegistro());
            if (sonConsecutivos && actual.esDiaDeDesgasteAlto() && anterior.esDiaDeDesgasteAlto()) {
                return true;
            }
        }
        return false;
    }
}