package com.rememberme.app.entity.enums;

public enum ViaAdministracion {
    ORAL("Vía Oral"),
    TOPICA("Tópica"),
    INTRAVENOSA("Intravenosa"),
    INTRAMUSCULAR("Intramuscular"),
    SUBCUTANEA("Subcutánea"),
    INHALATORIA("Inhalatoria"),
    OFTALMICA("Oftálmica"),
    OTICA("Ótica"),
    OTRA("Otra");

    private final String etiqueta;

    ViaAdministracion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}