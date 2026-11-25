package com.rememberme.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteDto {

    private Long id;

    @NotBlank(message = "El nombre completo del paciente es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombreCompleto;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @Size(max = 2000, message = "El texto de alergias es demasiado largo")
    private String alergias;

    @Size(max = 4000, message = "Las notas clínicas son demasiado largas")
    private String notasClinicas;
}