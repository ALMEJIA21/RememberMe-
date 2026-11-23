package com.rememberme.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bienestar_cuidador", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bc_usuario_fecha", columnNames = {"usuario_id", "fecha_registro"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienestarCuidador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "nivel_estres", nullable = false)
    private Integer nivelEstres;

    @Column(name = "nivel_fatiga", nullable = false)
    private Integer nivelFatiga;

    @Column(name = "horas_sueno", nullable = false, precision = 4, scale = 2)
    private BigDecimal horasSueno;

    @Column(name = "estado_emocional", length = 100)
    private String estadoEmocional;

    @Column(columnDefinition = "TEXT")
    private String nota;

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    public boolean esDiaDeDesgasteAlto() {
        return (nivelEstres != null && nivelEstres >= 4)
                || (nivelFatiga != null && nivelFatiga >= 4);
    }
}