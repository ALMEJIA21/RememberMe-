package com.rememberme.app.dto;

import com.rememberme.app.entity.enums.RolUsuario;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroUsuarioDto {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Ingresa un correo electrónico válido")
    @Size(max = 180, message = "El correo no puede superar los 180 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;

    @NotNull(message = "Debes seleccionar un tipo de cuenta")
    private RolUsuario rol;

    // Campos opcionales cuando el registro público es ROL_PACIENTE
    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @Size(max = 1000, message = "Las alergias no pueden superar los 1000 caracteres")
    private String alergias;
}