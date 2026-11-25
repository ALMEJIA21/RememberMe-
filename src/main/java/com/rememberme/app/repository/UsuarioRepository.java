package com.rememberme.app.repository;

import com.rememberme.app.entity.Usuario;
import com.rememberme.app.entity.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findByRolOrderByNombreAsc(RolUsuario rol);

    List<Usuario> findAllByOrderByCreadoEnDesc();

    long countByRol(RolUsuario rol);
}