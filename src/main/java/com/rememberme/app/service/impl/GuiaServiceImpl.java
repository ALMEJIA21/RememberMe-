package com.rememberme.app.service.impl;

import com.rememberme.app.dto.GuiaDto;
import com.rememberme.app.entity.CategoriaGuia;
import com.rememberme.app.entity.Guia;
import com.rememberme.app.exception.ResourceNotFoundException;
import com.rememberme.app.repository.CategoriaGuiaRepository;
import com.rememberme.app.repository.GuiaRepository;
import com.rememberme.app.service.GuiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GuiaServiceImpl implements GuiaService {

    private final GuiaRepository guiaRepository;
    private final CategoriaGuiaRepository categoriaGuiaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaGuia> listarCategoriasActivas() {
        return categoriaGuiaRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guia> buscarGuias(String termino, Long categoriaId) {
        if (termino != null && !termino.isBlank()) {
            List<Long> idsOrdenados = guiaRepository.buscarIdsFullText(termino.trim(), categoriaId);
            if (idsOrdenados.isEmpty()) {
                return List.of();
            }
            Map<Long, Integer> orden = new HashMap<>();
            for (int i = 0; i < idsOrdenados.size(); i++) {
                orden.put(idsOrdenados.get(i), i);
            }
            List<Guia> guias = guiaRepository.findByIdInWithCategoria(idsOrdenados);
            guias.sort(Comparator.comparingInt(g -> orden.getOrDefault(g.getId(), Integer.MAX_VALUE)));
            return guias;
        }

        if (categoriaId != null) {
            return guiaRepository.findByCategoriaIdWithCategoria(categoriaId);
        }
        return guiaRepository.findAllActivasWithCategoria();
    }

    @Override
    @Transactional(readOnly = true)
    public Guia obtenerPorId(Long guiaId) {
        return guiaRepository.findByIdWithCategoria(guiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Guía clínica no encontrada."));
    }

    @Override
    @Transactional
    public Guia publicarGuia(GuiaDto dto) {
        CategoriaGuia categoria = categoriaGuiaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de guía no encontrada."));

        return guiaRepository.save(Guia.builder()
                .categoria(categoria)
                .titulo(dto.getTitulo().trim())
                .contenido(dto.getContenido().trim())
                .etiquetas(dto.getEtiquetas() != null ? dto.getEtiquetas().trim() : null)
                .build());
    }
}