package com.rememberme.app.dto;

import com.rememberme.app.entity.enums.ViaAdministracion;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicamentoDto {

    private Long id;

    @NotNull(message = "Debes seleccionar el paciente")
    private Long pacienteId;

    @NotBlank(message = "El nombre del medicamento es obligatorio")
    @Size(max = 150, message = "El nombre del medicamento no puede superar los 150 caracteres")
    private String nombre;

    @Size(max = 150, message = "El principio activo no puede superar los 150 caracteres")
    private String principioActivo;

    @NotNull(message = "La dosis es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad de dosis debe ser mayor a 0")
    private BigDecimal cantidadDosis;

    @NotBlank(message = "La unidad de dosis es obligatoria (ej. mg, mL, UI)")
    @Size(max = 50, message = "La unidad no puede superar los 50 caracteres")
    private String unidadDosis;

    @NotNull(message = "La vía de administración es obligatoria")
    private ViaAdministracion viaAdministracion;

    @NotNull(message = "La frecuencia en horas es obligatoria")
    @Min(value = 1, message = "La frecuencia mínima es cada 1 hora")
    @Max(value = 720, message = "La frecuencia máxima permitida es 720 horas")
    private Integer frecuenciaHoras;

    private boolean esAltoRiesgo;

    @Size(max = 2000, message = "Las instrucciones son demasiado largas")
    private String instrucciones;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaHoraInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;
}