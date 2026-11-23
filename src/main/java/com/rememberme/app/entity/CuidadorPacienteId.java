package com.rememberme.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CuidadorPacienteId implements Serializable {

    @Column(name = "cuidador_id")
    private Long cuidadorId;

    @Column(name = "paciente_id")
    private Long pacienteId;
}