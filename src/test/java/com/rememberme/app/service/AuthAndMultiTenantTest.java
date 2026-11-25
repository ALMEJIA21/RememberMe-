package com.rememberme.app.service;

import com.rememberme.app.dto.RegistroUsuarioDto;
import com.rememberme.app.entity.Paciente;
import com.rememberme.app.entity.Usuario;
import com.rememberme.app.entity.enums.RolUsuario;
import com.rememberme.app.exception.ForbiddenException;
import com.rememberme.app.repository.CuidadorPacienteRepository;
import com.rememberme.app.repository.MedicamentoRepository;
import com.rememberme.app.repository.PacienteRepository;
import com.rememberme.app.repository.UsuarioRepository;
import com.rememberme.app.service.impl.AuthServiceImpl;
import com.rememberme.app.service.impl.PacienteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthAndMultiTenantTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CuidadorPacienteRepository cuidadorPacienteRepository;
    @Mock
    private MedicamentoRepository medicamentoRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @InjectMocks
    private PacienteServiceImpl pacienteService;

    @Test
    void registroPublicoDebeRechazarRolAdmin() {
        RegistroUsuarioDto dto = RegistroUsuarioDto.builder()
                .nombre("Intento Admin")
                .correo("hacker@rememberme.care")
                .contrasena("Secreta123")
                .rol(RolUsuario.ROL_ADMIN)
                .build();

        assertThrows(ForbiddenException.class, () -> authService.registrarUsuarioPublico(dto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registroPublicoComoPacienteDebeCrearSuFilaEnPacientes() {
        RegistroUsuarioDto dto = RegistroUsuarioDto.builder()
                .nombre("Laura Gómez")
                .correo("laura@rememberme.care")
                .contrasena("Paciente2026!")
                .rol(RolUsuario.ROL_PACIENTE)
                .alergias("Penicilina")
                .build();

        when(usuarioRepository.existsByCorreoIgnoreCase("laura@rememberme.care")).thenReturn(false);
        when(passwordEncoder.encode("Paciente2026!")).thenReturn("encoded-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(50L);
            return u;
        });
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = authService.registrarUsuarioPublico(dto);

        assertEquals(RolUsuario.ROL_PACIENTE, creado.getRol());
        verify(pacienteRepository, times(1)).save(argThat(p ->
                p.getUsuario().getId().equals(50L)
                        && "Laura Gómez".equals(p.getNombreCompleto())
                        && "Penicilina".equals(p.getAlergias())
                        && Boolean.TRUE.equals(p.getActivo())));
    }

    @Test
    void cuidadorNoVinculadoNoPuedeConsultarPacienteAjeno() {
        Paciente ajeno = Paciente.builder().id(88L).nombreCompleto("Paciente Ajeno").activo(true).build();
        when(pacienteRepository.findByIdAndActivoTrue(88L)).thenReturn(Optional.of(ajeno));
        when(cuidadorPacienteRepository.existsByIdCuidadorIdAndIdPacienteId(10L, 88L)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> pacienteService.obtenerPacienteParaCuidador(88L, 10L));
    }
}