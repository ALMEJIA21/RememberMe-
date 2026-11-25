package com.rememberme.app.repository;

import com.rememberme.app.entity.BienestarCuidador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BienestarCuidadorRepository extends JpaRepository<BienestarCuidador, Long> {

    Optional<BienestarCuidador> findByUsuarioIdAndFechaRegistro(Long usuarioId, LocalDate fechaRegistro);

    List<BienestarCuidador> findTop14ByUsuarioIdOrderByFechaRegistroDesc(Long usuarioId);

    List<BienestarCuidador> findByUsuarioIdAndFechaRegistroGreaterThanEqualOrderByFechaRegistroDesc(
            Long usuarioId, LocalDate desde);

    @Query("""
            SELECT b FROM BienestarCuidador b
            JOIN FETCH b.usuario u
            WHERE b.fechaRegistro >= :desde
            ORDER BY u.id ASC, b.fechaRegistro DESC
            """)
    List<BienestarCuidador> findRecientesConUsuario(@Param("desde") LocalDate desde);
}