package com.rememberme.app.service;

import com.rememberme.app.dto.GuiaDto;
import com.rememberme.app.entity.CategoriaGuia;
import com.rememberme.app.entity.Guia;

import java.util.List;

public interface GuiaService {

    List<CategoriaGuia> listarCategoriasActivas();

    List<Guia> buscarGuias(String termino, Long categoriaId);

    Guia obtenerPorId(Long guiaId);

    Guia publicarGuia(GuiaDto dto);
}