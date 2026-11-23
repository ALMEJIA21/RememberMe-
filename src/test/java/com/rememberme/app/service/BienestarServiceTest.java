package com.rememberme.app.service;

import com.rememberme.app.entity.BienestarCuidador;
import com.rememberme.app.repository.BienestarCuidadorRepository;
import com.rememberme.app.repository.UsuarioRepository;
import com.rememberme.app.service.impl.BienestarServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BienestarServiceTest {

    @Mock
    private BienestarCuidadorRepository bienestarCuidadorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BienestarServiceImpl bienestarService;

    @Test
    void debeActivarAlertaCuandoHayDosDiasConsecutivosDeDesgasteAlto() {
        LocalDate hoy = LocalDate.now();
        List<BienestarCuidador> registros = List.of(
                BienestarCuidador.builder().fechaRegistro(hoy).nivelEstres(4).nivelFatiga(3).horasSueno(new BigDecimal("5.5")).build(),
                BienestarCuidador.builder().fechaRegistro(hoy.minusDays(1)).nivelEstres(3).nivelFatiga(5).horasSueno(new BigDecimal("5.0")).build()
        );

        when(bienestarCuidadorRepository
                .findByUsuarioIdAndFechaRegistroGreaterThanEqualOrderByFechaRegistroDesc(eq(10L), any(LocalDate.class)))
                .thenReturn(registros);

        assertTrue(bienestarService.tieneAlertaDesgasteConsecutivo(10L));
    }

    @Test
    void noDebeActivarAlertaSiLosDiasDeDesgasteNoSonConsecutivos() {
        LocalDate hoy = LocalDate.now();
        List<BienestarCuidador> registros = List.of(
                BienestarCuidador.builder().fechaRegistro(hoy).nivelEstres(4).nivelFatiga(4).horasSueno(new BigDecimal("6.0")).build(),
                BienestarCuidador.builder().fechaRegistro(hoy.minusDays(1)).nivelEstres(2).nivelFatiga(2).horasSueno(new BigDecimal("8.0")).build(),
                BienestarCuidador.builder().fechaRegistro(hoy.minusDays(2)).nivelEstres(5).nivelFatiga(5).horasSueno(new BigDecimal("5.0")).build()
        );

        when(bienestarCuidadorRepository
                .findByUsuarioIdAndFechaRegistroGreaterThanEqualOrderByFechaRegistroDesc(eq(10L), any(LocalDate.class)))
                .thenReturn(registros);

        assertFalse(bienestarService.tieneAlertaDesgasteConsecutivo(10L));
    }
}