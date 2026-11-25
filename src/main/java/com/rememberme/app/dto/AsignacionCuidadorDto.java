package com.rememberme.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionCuidadorDto {

    @NotNull(message = "Debes seleccionar un cuidador")
    private Long cuidadorId;

    @NotNull(message = "Debes seleccionar un paciente")
    private Long pacienteId;

    private boolean esPrincipal;
}