package com.rememberme.app.repository;

import com.rememberme.app.entity.CategoriaGuia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaGuiaRepository extends JpaRepository<CategoriaGuia, Long> {

    List<CategoriaGuia> findByActivoTrueOrderByNombreAsc();
}