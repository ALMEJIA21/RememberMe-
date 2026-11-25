package com.rememberme.app.service;

import com.rememberme.app.dto.RegistroUsuarioDto;
import com.rememberme.app.entity.Usuario;

public interface AuthService {

    Usuario registrarUsuarioPublico(RegistroUsuarioDto dto);
}