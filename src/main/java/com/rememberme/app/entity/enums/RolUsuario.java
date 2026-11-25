package com.rememberme.app.entity.enums;

public enum RolUsuario {
    ROL_CUIDADOR("Cuidador"),
    ROL_PACIENTE("Paciente"),
    ROL_ADMIN("Administrador");

    private final String etiqueta;

    RolUsuario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}