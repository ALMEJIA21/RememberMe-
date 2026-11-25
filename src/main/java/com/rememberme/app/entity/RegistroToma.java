package com.rememberme.app.entity;

import com.rememberme.app.entity.enums.EstadoToma;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_tomas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_toma_med_hora", columnNames = {"medicamento_id", "hora_programada"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroToma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrado_por_usuario_id")
    private Usuario administradoPor;

    @Column(name = "hora_programada", nullable = false)
    private LocalDateTime horaProgramada;

    @Column(name = "hora_real")
    private LocalDateTime horaReal;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        columnDefinition = "ENUM('PENDIENTE', 'ADMINISTRADO', 'OMITIDO', 'RETRASADO')"
    )
    private EstadoToma estado = EstadoToma.PENDIENTE;

    @Column(name = "motivo_omision", length = 500)
    private String motivoOmision;

    @Builder.Default
    @Column(name = "verificacion_paciente", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean verificacionPaciente = false;

    @Builder.Default
    @Column(name = "verificacion_medicamento", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean verificacionMedicamento = false;

    @Builder.Default
    @Column(name = "verificacion_dosis", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean verificacionDosis = false;

    @Builder.Default
    @Column(name = "verificacion_via", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean verificacionVia = false;

    @Builder.Default
    @Column(name = "verificacion_hora", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean verificacionHora = false;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    public boolean cumpleCincoCorrectos() {
        return Boolean.TRUE.equals(verificacionPaciente)
                && Boolean.TRUE.equals(verificacionMedicamento)
                && Boolean.TRUE.equals(verificacionDosis)
                && Boolean.TRUE.equals(verificacionVia)
                && Boolean.TRUE.equals(verificacionHora);
    }
}