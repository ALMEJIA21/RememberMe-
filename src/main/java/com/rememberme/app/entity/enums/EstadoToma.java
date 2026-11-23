package com.rememberme.app.entity.enums;

public enum EstadoToma {
    PENDIENTE("Pendiente"),
    ADMINISTRADO("Administrado"),
    OMITIDO("Omitido"),
    RETRASADO("Retrasado");

    private final String etiqueta;

    EstadoToma(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}