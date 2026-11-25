package com.rememberme.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaDto {

    @NotNull(message = "Debes seleccionar una categoría")
    private Long categoriaId;

    @NotBlank(message = "El título de la guía es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @NotBlank(message = "El contenido de la guía es obligatorio")
    private String contenido;

    @Size(max = 255, message = "Las etiquetas no pueden superar los 255 caracteres")
    private String etiquetas;
}