package com.rememberme.app.service.impl;

import com.rememberme.app.dto.RegistroUsuarioDto;
import com.rememberme.app.entity.Paciente;
import com.rememberme.app.entity.Usuario;
import com.rememberme.app.entity.enums.RolUsuario;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.exception.ForbiddenException;
import com.rememberme.app.repository.PacienteRepository;
import com.rememberme.app.repository.UsuarioRepository;
import com.rememberme.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Usuario registrarUsuarioPublico(RegistroUsuarioDto dto) {
        if (dto.getRol() == null || dto.getRol() == RolUsuario.ROL_ADMIN) {
            throw new ForbiddenException("El registro público solo permite cuentas de Cuidador o Paciente.");
        }

        String correoNormalizado = dto.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            throw new BusinessRuleException("Ya existe una cuenta registrada con el correo: " + correoNormalizado);
        }

        Usuario nuevoUsuario = usuarioRepository.save(Usuario.builder()
                .nombre(dto.getNombre().trim())
                .correo(correoNormalizado)
                .contrasena(passwordEncoder.encode(dto.getContrasena()))
                .rol(dto.getRol())
                .build());

        // Si el usuario se registra como ROL_PACIENTE, se crea automáticamente su ficha en pacientes
        if (dto.getRol() == RolUsuario.ROL_PACIENTE) {
            pacienteRepository.save(Paciente.builder()
                    .usuario(nuevoUsuario)
                    .nombreCompleto(nuevoUsuario.getNombre())
                    .fechaNacimiento(dto.getFechaNacimiento())
                    .alergias(dto.getAlergias())
                    .activo(true)
                    .build());
        }

        return nuevoUsuario;
    }
}