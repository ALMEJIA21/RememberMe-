package com.rememberme.app.dto;

import com.rememberme.app.entity.enums.EstadoToma;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TomaVerificacionDto {

    @NotNull(message = "Debes indicar el estado resultante de la toma")
    private EstadoToma estadoDestino;

    private boolean verificacionPaciente;
    private boolean verificacionMedicamento;
    private boolean verificacionDosis;
    private boolean verificacionVia;
    private boolean verificacionHora;

    @Size(max = 500, message = "El motivo de omisión no puede superar los 500 caracteres")
    private String motivoOmision;

    @Size(max = 2000, message = "Las notas adicionales no pueden superar los 2000 caracteres")
    private String notas;
}