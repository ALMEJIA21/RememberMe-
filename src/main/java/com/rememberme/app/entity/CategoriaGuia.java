package com.rememberme.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias_guias", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cat_guia_nombre", columnNames = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaGuia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean activo = true;
}