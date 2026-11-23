package com.rememberme.app.security;

import com.rememberme.app.entity.Usuario;
import com.rememberme.app.entity.enums.RolUsuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String nombre;
    private final String correo;
    private final String contrasena;
    private final RolUsuario rol;
    private final String avatarUrl;
    private final String iniciales;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.correo = usuario.getCorreo();
        this.contrasena = usuario.getContrasena();
        this.rol = usuario.getRol();
        this.avatarUrl = usuario.getAvatarUrl();
        this.iniciales = usuario.getIniciales();
        this.authorities = List.of(new SimpleGrantedAuthority(usuario.getRol().name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}