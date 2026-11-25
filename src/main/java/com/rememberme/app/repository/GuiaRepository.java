package com.rememberme.app.repository;

import com.rememberme.app.entity.Guia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuiaRepository extends JpaRepository<Guia, Long> {

    @Query("""
            SELECT g FROM Guia g
            JOIN FETCH g.categoria c
            WHERE c.activo = true
            ORDER BY g.creadoEn DESC
            """)
    List<Guia> findAllActivasWithCategoria();

    @Query("""
            SELECT g FROM Guia g
            JOIN FETCH g.categoria c
            WHERE c.id = :categoriaId AND c.activo = true
            ORDER BY g.creadoEn DESC
            """)
    List<Guia> findByCategoriaIdWithCategoria(@Param("categoriaId") Long categoriaId);

    @Query("""
            SELECT g FROM Guia g
            JOIN FETCH g.categoria c
            WHERE g.id IN :ids
            """)
    List<Guia> findByIdInWithCategoria(@Param("ids") List<Long> ids);

    @Query("""
            SELECT g FROM Guia g
            JOIN FETCH g.categoria c
            WHERE g.id = :id
            """)
    Optional<Guia> findByIdWithCategoria(@Param("id") Long id);

    @Query(value = """
            SELECT g.id
            FROM guias g
            INNER JOIN categorias_guias c ON c.id = g.categoria_id
            WHERE c.activo = 1
              AND (:categoriaId IS NULL OR g.categoria_id = :categoriaId)
              AND MATCH (g.titulo, g.contenido, g.etiquetas) AGAINST (:termino IN NATURAL LANGUAGE MODE)
            ORDER BY MATCH(g.titulo, g.contenido, g.etiquetas) AGAINST (:termino IN NATURAL LANGUAGE MODE) DESC
            """, nativeQuery = true)
    List<Long> buscarIdsFullText(@Param("termino") String termino,
                                 @Param("categoriaId") Long categoriaId);
}