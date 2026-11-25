package com.rememberme.app.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienestarDto {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaRegistro;

    @NotNull(message = "El nivel de estrés es obligatorio")
    @Min(value = 1, message = "El nivel de estrés mínimo es 1")
    @Max(value = 5, message = "El nivel de estrés máximo es 5")
    private Integer nivelEstres;

    @NotNull(message = "El nivel de fatiga es obligatorio")
    @Min(value = 1, message = "El nivel de fatiga mínimo es 1")
    @Max(value = 5, message = "El nivel de fatiga máximo es 5")
    private Integer nivelFatiga;

    @NotNull(message = "Las horas de sueño son obligatorias")
    @DecimalMin(value = "0.0", message = "Las horas de sueño no pueden ser negativas")
    @DecimalMax(value = "24.0", message = "Las horas de sueño no pueden superar las 24 horas")
    private BigDecimal horasSueno;

    @Size(max = 100, message = "El estado emocional no puede superar los 100 caracteres")
    private String estadoEmocional;

    @Size(max = 2000, message = "La nota personal es demasiado larga")
    private String nota;
}