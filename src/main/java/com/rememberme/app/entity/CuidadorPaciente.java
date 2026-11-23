package com.rememberme.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuidador_paciente")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuidadorPaciente {

    @EmbeddedId
    private CuidadorPacienteId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("cuidadorId")
    @JoinColumn(name = "cuidador_id", nullable = false)
    private Usuario cuidador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("pacienteId")
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Builder.Default
    @Column(name = "es_principal", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean esPrincipal = false;

    @CreatedDate
    @Column(name = "asignado_en", nullable = false, updatable = false)
    private LocalDateTime asignadoEn;
}