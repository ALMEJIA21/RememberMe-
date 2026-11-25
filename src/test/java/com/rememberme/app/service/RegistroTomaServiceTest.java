package com.rememberme.app.service;

import com.rememberme.app.dto.TomaVerificacionDto;
import com.rememberme.app.entity.Medicamento;
import com.rememberme.app.entity.Paciente;
import com.rememberme.app.entity.RegistroToma;
import com.rememberme.app.entity.Usuario;
import com.rememberme.app.entity.enums.EstadoToma;
import com.rememberme.app.entity.enums.RolUsuario;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.exception.ForbiddenException;
import com.rememberme.app.repository.CuidadorPacienteRepository;
import com.rememberme.app.repository.RegistroTomaRepository;
import com.rememberme.app.repository.UsuarioRepository;
import com.rememberme.app.security.CustomUserDetails;
import com.rememberme.app.service.impl.RegistroTomaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroTomaServiceTest {

    @Mock
    private RegistroTomaRepository registroTomaRepository;

    @Mock
    private CuidadorPacienteRepository cuidadorPacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RegistroTomaServiceImpl registroTomaService;

    private Usuario cuidador;
    private Usuario otroPacienteUsuario;
    private Paciente paciente;
    private RegistroToma toma;

    @BeforeEach
    void setUp() {
        cuidador = Usuario.builder()
                .id(10L)
                .nombre("Sofía Mendoza")
                .correo("sofia@rememberme.care")
                .contrasena("hash")
                .rol(RolUsuario.ROL_CUIDADOR)
                .build();

        otroPacienteUsuario = Usuario.builder()
                .id(99L)
                .nombre("Otro Paciente")
                .correo("otro@rememberme.care")
                .contrasena("hash")
                .rol(RolUsuario.ROL_PACIENTE)
                .build();

        Usuario duenoPaciente = Usuario.builder()
                .id(20L)
                .nombre("Elena Morales")
                .correo("elena@rememberme.care")
                .contrasena("hash")
                .rol(RolUsuario.ROL_PACIENTE)
                .build();

        paciente = Paciente.builder()
                .id(5L)
                .usuario(duenoPaciente)
                .nombreCompleto("Elena Morales")
                .activo(true)
                .build();

        Medicamento medicamento = Medicamento.builder()
                .id(3L)
                .paciente(paciente)
                .nombre("Losartán")
                .activo(true)
                .build();

        toma = RegistroToma.builder()
                .id(1L)
                .medicamento(medicamento)
                .horaProgramada(LocalDateTime.now())
                .estado(EstadoToma.PENDIENTE)
                .build();
    }

    @Test
    void debeRechazarAdministradoSiFaltaAlgunoDeLosCincoCorrectos() {
        CustomUserDetails principal = new CustomUserDetails(cuidador);
        when(registroTomaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(toma));
        when(cuidadorPacienteRepository.existsByIdCuidadorIdAndIdPacienteId(10L, 5L)).thenReturn(true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(cuidador));

        TomaVerificacionDto dtoIncompleto = TomaVerificacionDto.builder()
                .estadoDestino(EstadoToma.ADMINISTRADO)
                .verificacionPaciente(true)
                .verificacionMedicamento(true)
                .verificacionDosis(true)
                .verificacionVia(true)
                .verificacionHora(false) // Falta la hora correcta
                .build();

        assertThrows(BusinessRuleException.class,
                () -> registroTomaService.verificarYRegistrarToma(1L, dtoIncompleto, principal));

        verify(registroTomaRepository, never()).save(any());
    }

    @Test
    void debeMarcarAdministradoCuandoCumpleCincoCorrectos() {
        CustomUserDetails principal = new CustomUserDetails(cuidador);
        when(registroTomaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(toma));
        when(cuidadorPacienteRepository.existsByIdCuidadorIdAndIdPacienteId(10L, 5L)).thenReturn(true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(cuidador));
        when(registroTomaRepository.save(any(RegistroToma.class))).thenAnswer(inv -> inv.getArgument(0));

        TomaVerificacionDto dtoCompleto = TomaVerificacionDto.builder()
                .estadoDestino(EstadoToma.ADMINISTRADO)
                .verificacionPaciente(true)
                .verificacionMedicamento(true)
                .verificacionDosis(true)
                .verificacionVia(true)
                .verificacionHora(true)
                .notas("Sin novedades")
                .build();

        RegistroToma resultado = registroTomaService.verificarYRegistrarToma(1L, dtoCompleto, principal);

        assertEquals(EstadoToma.ADMINISTRADO, resultado.getEstado());
        assertTrue(resultado.cumpleCincoCorrectos());
        assertNotNull(resultado.getHoraReal());
        assertEquals(cuidador, resultado.getAdministradoPor());
    }

    @Test
    void debeExigirMotivoCuandoSeOmiteUnaToma() {
        CustomUserDetails principal = new CustomUserDetails(cuidador);
        when(registroTomaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(toma));
        when(cuidadorPacienteRepository.existsByIdCuidadorIdAndIdPacienteId(10L, 5L)).thenReturn(true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(cuidador));

        TomaVerificacionDto dtoSinMotivo = TomaVerificacionDto.builder()
                .estadoDestino(EstadoToma.OMITIDO)
                .motivoOmision("   ")
                .build();

        assertThrows(BusinessRuleException.class,
                () -> registroTomaService.verificarYRegistrarToma(1L, dtoSinMotivo, principal));
        verify(registroTomaRepository, never()).save(any());
    }

    @Test
    void debeLanzarForbiddenSiCuidadorNoEstaVinculadoAlPaciente() {
        CustomUserDetails principal = new CustomUserDetails(cuidador);
        when(registroTomaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(toma));
        when(cuidadorPacienteRepository.existsByIdCuidadorIdAndIdPacienteId(10L, 5L)).thenReturn(false);

        TomaVerificacionDto dto = TomaVerificacionDto.builder()
                .estadoDestino(EstadoToma.ADMINISTRADO)
                .build();

        assertThrows(ForbiddenException.class,
                () -> registroTomaService.verificarYRegistrarToma(1L, dto, principal));
    }

    @Test
    void debeLanzarForbiddenSiPacienteIntentaConfirmarTomaAjena() {
        CustomUserDetails principalAjeno = new CustomUserDetails(otroPacienteUsuario);
        when(registroTomaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(toma));

        TomaVerificacionDto dto = TomaVerificacionDto.builder()
                .estadoDestino(EstadoToma.ADMINISTRADO)
                .build();

        assertThrows(ForbiddenException.class,
                () -> registroTomaService.verificarYRegistrarToma(1L, dto, principalAjeno));
    }
}