package com.rememberme.app.config;

import com.rememberme.app.entity.*;
import com.rememberme.app.entity.enums.EstadoToma;
import com.rememberme.app.entity.enums.RolUsuario;
import com.rememberme.app.entity.enums.ViaAdministracion;
import com.rememberme.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final CuidadorPacienteRepository cuidadorPacienteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final RegistroTomaRepository registroTomaRepository;
    private final BienestarCuidadorRepository bienestarCuidadorRepository;
    private final CategoriaGuiaRepository categoriaGuiaRepository;
    private final GuiaRepository guiaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("DataSeeder omitido: ya existen registros en rememberme_db.");
            return;
        }

        log.info("Iniciando semilla clínica de RememberMe...");

        // 1. Usuarios iniciales (Admin, 2 Cuidadores, 1 Paciente)
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .nombre("Dr. Carlos Vega")
                .correo("admin@rememberme.care")
                .contrasena(passwordEncoder.encode("Admin2026!"))
                .rol(RolUsuario.ROL_ADMIN)
                .build());

        Usuario sofia = usuarioRepository.save(Usuario.builder()
                .nombre("Sofía Mendoza")
                .correo("sofia.mendoza@rememberme.care")
                .contrasena(passwordEncoder.encode("Cuidador2026!"))
                .rol(RolUsuario.ROL_CUIDADOR)
                .avatarUrl("/images/avatar-cuidador-sofia.jpg")
                .build());

        Usuario mateo = usuarioRepository.save(Usuario.builder()
                .nombre("Mateo Ríos")
                .correo("mateo.rios@rememberme.care")
                .contrasena(passwordEncoder.encode("Cuidador2026!"))
                .rol(RolUsuario.ROL_CUIDADOR)
                .build());

        Usuario usuarioElena = usuarioRepository.save(Usuario.builder()
                .nombre("Elena Morales")
                .correo("elena.morales@rememberme.care")
                .contrasena(passwordEncoder.encode("Paciente2026!"))
                .rol(RolUsuario.ROL_PACIENTE)
                .avatarUrl("/images/avatar-paciente-elena.jpg")
                .build());

        // 2. Pacientes
        Paciente elena = pacienteRepository.save(Paciente.builder()
                .usuario(usuarioElena)
                .nombreCompleto("Elena Morales")
                .fechaNacimiento(LocalDate.of(1948, 5, 14))
                .alergias("Penicilina, Sulfonamidas")
                .notasClinicas("Hipertensión arterial controlada y diabetes tipo 2. Monitorear glucemia antes de la cena.")
                .activo(true)
                .avatarUrl("/images/avatar-paciente-elena.jpg")
                .build());

        Paciente arturo = pacienteRepository.save(Paciente.builder()
                .nombreCompleto("Arturo Benítez")
                .fechaNacimiento(LocalDate.of(1942, 11, 3))
                .alergias("AINEs (Ibuprofeno, Naproxeno)")
                .notasClinicas("Fibrilación auricular anticoagulada. Vigilar signos de sangrado o hematomas.")
                .activo(true)
                .build());

        // 3. Vínculos Cuidador <-> Paciente
        cuidadorPacienteRepository.saveAll(List.of(
                CuidadorPaciente.builder()
                        .id(new CuidadorPacienteId(sofia.getId(), elena.getId()))
                        .cuidador(sofia)
                        .paciente(elena)
                        .esPrincipal(true)
                        .build(),
                CuidadorPaciente.builder()
                        .id(new CuidadorPacienteId(sofia.getId(), arturo.getId()))
                        .cuidador(sofia)
                        .paciente(arturo)
                        .esPrincipal(true)
                        .build(),
                CuidadorPaciente.builder()
                        .id(new CuidadorPacienteId(mateo.getId(), elena.getId()))
                        .cuidador(mateo)
                        .paciente(elena)
                        .esPrincipal(false)
                        .build()
        ));

        // 4. Medicamentos
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioBase = hoy.minusDays(5).atTime(8, 0);

        Medicamento losartan = medicamentoRepository.save(Medicamento.builder()
                .paciente(elena)
                .nombre("Losartán Potásico")
                .principioActivo("Losartán")
                .cantidadDosis(new BigDecimal("50"))
                .unidadDosis("mg")
                .viaAdministracion(ViaAdministracion.ORAL)
                .frecuenciaHoras(12)
                .esAltoRiesgo(false)
                .instrucciones("Administrar con un vaso de agua. Controlar presión arterial si refiere mareo.")
                .fechaHoraInicio(inicioBase)
                .activo(true)
                .build());

        Medicamento insulina = medicamentoRepository.save(Medicamento.builder()
                .paciente(elena)
                .nombre("Insulina Glargina")
                .principioActivo("Insulina glargina")
                .cantidadDosis(new BigDecimal("14"))
                .unidadDosis("UI")
                .viaAdministracion(ViaAdministracion.SUBCUTANEA)
                .frecuenciaHoras(24)
                .esAltoRiesgo(true)
                .instrucciones("Rotar sitio de aplicación abdominal. Verificar glucemia capilar antes de aplicar.")
                .fechaHoraInicio(hoy.minusDays(5).atTime(20, 0))
                .activo(true)
                .build());

        Medicamento metformina = medicamentoRepository.save(Medicamento.builder()
                .paciente(elena)
                .nombre("Metformina")
                .principioActivo("Clorhidrato de metformina")
                .cantidadDosis(new BigDecimal("850"))
                .unidadDosis("mg")
                .viaAdministracion(ViaAdministracion.ORAL)
                .frecuenciaHoras(12)
                .esAltoRiesgo(false)
                .instrucciones("Tomar inmediatamente después de las comidas principales.")
                .fechaHoraInicio(hoy.minusDays(5).atTime(8, 30))
                .activo(true)
                .build());

        Medicamento warfarina = medicamentoRepository.save(Medicamento.builder()
                .paciente(arturo)
                .nombre("Warfarina Sódica")
                .principioActivo("Warfarina")
                .cantidadDosis(new BigDecimal("5"))
                .unidadDosis("mg")
                .viaAdministracion(ViaAdministracion.ORAL)
                .frecuenciaHoras(24)
                .esAltoRiesgo(true)
                .instrucciones("Alto riesgo hemorrágico. No duplicar dosis si se omite; reportar sangrado nasal o gingival.")
                .fechaHoraInicio(hoy.minusDays(5).atTime(18, 0))
                .activo(true)
                .build());

        // 5. Agenda de tomas de HOY
        registroTomaRepository.saveAll(List.of(
                RegistroToma.builder()
                        .medicamento(losartan)
                        .administradoPor(sofia)
                        .horaProgramada(hoy.atTime(LocalTime.of(8, 0)))
                        .horaReal(hoy.atTime(LocalTime.of(8, 5)))
                        .estado(EstadoToma.ADMINISTRADO)
                        .verificacionPaciente(true)
                        .verificacionMedicamento(true)
                        .verificacionDosis(true)
                        .verificacionVia(true)
                        .verificacionHora(true)
                        .notas("Presión arterial previa 124/78 mmHg.")
                        .build(),
                RegistroToma.builder()
                        .medicamento(metformina)
                        .administradoPor(sofia)
                        .horaProgramada(hoy.atTime(LocalTime.of(8, 30)))
                        .horaReal(hoy.atTime(LocalTime.of(8, 35)))
                        .estado(EstadoToma.ADMINISTRADO)
                        .verificacionPaciente(true)
                        .verificacionMedicamento(true)
                        .verificacionDosis(true)
                        .verificacionVia(true)
                        .verificacionHora(true)
                        .notas("Administrada tras el desayuno.")
                        .build(),
                RegistroToma.builder()
                        .medicamento(warfarina)
                        .horaProgramada(hoy.atTime(LocalTime.of(18, 0)))
                        .estado(EstadoToma.PENDIENTE)
                        .build(),
                RegistroToma.builder()
                        .medicamento(losartan)
                        .horaProgramada(hoy.atTime(LocalTime.of(20, 0)))
                        .estado(EstadoToma.PENDIENTE)
                        .build(),
                RegistroToma.builder()
                        .medicamento(insulina)
                        .horaProgramada(hoy.atTime(LocalTime.of(21, 0)))
                        .estado(EstadoToma.PENDIENTE)
                        .build()
        ));

        // 6. Bienestar del cuidador (2 días consecutivos altos para Sofía -> activa alerta preventiva)
        bienestarCuidadorRepository.saveAll(List.of(
                BienestarCuidador.builder()
                        .usuario(sofia)
                        .fechaRegistro(hoy.minusDays(2))
                        .nivelEstres(4)
                        .nivelFatiga(4)
                        .horasSueno(new BigDecimal("5.5"))
                        .estadoEmocional("Agotada")
                        .nota("Noche con múltiples despertares por control de glucemia.")
                        .build(),
                BienestarCuidador.builder()
                        .usuario(sofia)
                        .fechaRegistro(hoy.minusDays(1))
                        .nivelEstres(5)
                        .nivelFatiga(4)
                        .horasSueno(new BigDecimal("5.0"))
                        .estadoEmocional("Sobrecargada")
                        .nota("Sensación de tensión muscular y fatiga acumulada.")
                        .build(),
                BienestarCuidador.builder()
                        .usuario(mateo)
                        .fechaRegistro(hoy.minusDays(1))
                        .nivelEstres(2)
                        .nivelFatiga(2)
                        .horasSueno(new BigDecimal("7.5"))
                        .estadoEmocional("Sereno")
                        .nota("Turno de apoyo sin novedades.")
                        .build()
        ));

        // 7. Categorías y Guías Clínicas (para búsqueda FULLTEXT)
        CategoriaGuia catProtocolo = categoriaGuiaRepository.save(CategoriaGuia.builder()
                .nombre("Protocolo de Medicación")
                .descripcion("Estándares de seguridad y verificación de dosis en el hogar")
                .activo(true)
                .build());

        CategoriaGuia catAutocuidado = categoriaGuiaRepository.save(CategoriaGuia.builder()
                .nombre("Autocuidado del Cuidador")
                .descripcion("Prevención de desgaste físico y fatiga emocional del cuidador")
                .activo(true)
                .build());

        CategoriaGuia catAlarma = categoriaGuiaRepository.save(CategoriaGuia.builder()
                .nombre("Signos de Alarma")
                .descripcion("Identificación temprana de reacciones adversas y riesgo farmacológico")
                .activo(true)
                .build());

        guiaRepository.saveAll(List.of(
                Guia.builder()
                        .categoria(catProtocolo)
                        .titulo("Protocolo de los 5 Correctos antes de administrar fármacos")
                        .contenido("Antes de administrar cualquier medicamento en el hogar verifique sistemáticamente: "
                                + "1) Paciente correcto confirmando identidad y alergias; "
                                + "2) Medicamento correcto leyendo la etiqueta original y principio activo; "
                                + "3) Dosis correcta usando jeringas o dosificadores calibrados; "
                                + "4) Vía de administración correcta; "
                                + "5) Hora correcta respetando los intervalos prescritos.")
                        .etiquetas("5 correctos, seguridad, dosificacion, protocolo, administracion")
                        .build(),
                Guia.builder()
                        .categoria(catAlarma)
                        .titulo("Manejo seguro de medicamentos de alto riesgo: Insulina y Anticoagulantes")
                        .contenido("Los fármacos de alto riesgo como la insulina subcutánea y la warfarina requieren doble verificación. "
                                + "Nunca duplique una dosis olvidada de anticoagulante. En insulina, rote las zonas de punción abdominal "
                                + "y vigile signos de hipoglucemia como sudoración fría, temblor o confusión.")
                        .etiquetas("alto riesgo, insulina, warfarina, anticoagulante, hipoglucemia, sangrado")
                        .build(),
                Guia.builder()
                        .categoria(catAutocuidado)
                        .titulo("Prevención del síndrome de desgaste (Burnout) en cuidadores familiares")
                        .contenido("Cuando el nivel de estrés o fatiga supera 4/5 durante dos días consecutivos es necesario activar red de apoyo. "
                                + "Delegue tomas secundarias a un cuidador de apoyo, priorice bloques de descanso nocturno continuo "
                                + "y realice pausas activas respiratorias de 10 minutos entre rondas de medicación.")
                        .etiquetas("burnout, fatiga, estres, sueno, relevo, autocuidado, cuidador")
                        .build()
        ));

        log.info("Semilla clínica de RememberMe completada (Admin ID: {}).", admin.getId());
    }
}