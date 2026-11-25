package com.rememberme.app.entity;

import com.rememberme.app.entity.enums.ViaAdministracion;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicamentos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "principio_activo", length = 150)
    private String principioActivo;

    @Column(name = "cantidad_dosis", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadDosis;

    @Column(name = "unidad_dosis", nullable = false, length = 50)
    private String unidadDosis;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "via_administracion",
        nullable = false,
        columnDefinition = "ENUM('ORAL', 'TOPICA', 'INTRAVENOSA', 'INTRAMUSCULAR', 'SUBCUTANEA', 'INHALATORIA', 'OFTALMICA', 'OTICA', 'OTRA')"
    )
    private ViaAdministracion viaAdministracion;

    @Column(name = "frecuencia_horas", nullable = false)
    private Integer frecuenciaHoras;

    @Builder.Default
    @Column(name = "es_alto_riesgo", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean esAltoRiesgo = false;

    @Column(columnDefinition = "TEXT")
    private String instrucciones;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    public String getDosisFormateada() {
        if (cantidadDosis == null) {
            return "";
        }
        String num = cantidadDosis.stripTrailingZeros().toPlainString();
        return num + " " + (unidadDosis != null ? unidadDosis : "");
    }
}