package com.rememberme.app.controller;

import com.rememberme.app.dto.PacienteDto;
import com.rememberme.app.dto.TomaVerificacionDto;
import com.rememberme.app.entity.CuidadorPaciente;
import com.rememberme.app.entity.Medicamento;
import com.rememberme.app.entity.Paciente;
import com.rememberme.app.entity.RegistroToma;
import com.rememberme.app.entity.enums.EstadoToma;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.security.CustomUserDetails;
import com.rememberme.app.service.MedicamentoService;
import com.rememberme.app.service.PacienteService;
import com.rememberme.app.service.RegistroTomaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/paciente")
@RequiredArgsConstructor
public class PacienteController {

    private static final double CIRCUNFERENCIA_ANILLO = 2 * Math.PI * 28.0;

    private final PacienteService pacienteService;
    private final RegistroTomaService registroTomaService;
    private final MedicamentoService medicamentoService;

    @GetMapping("/mi-plan")
    public String miPlan(@AuthenticationPrincipal CustomUserDetails usuario, Model model) {
        Paciente paciente = pacienteService.obtenerExpedientePropio(usuario.getId());
        List<RegistroToma> tomasHoy = registroTomaService.obtenerAgendaHoyPaciente(usuario.getId());

        int porcentaje = registroTomaService.calcularPorcentajeAdherencia(tomasHoy);
        double dashOffset = CIRCUNFERENCIA_ANILLO * (1.0 - (porcentaje / 100.0));

        long administradas = tomasHoy.stream().filter(t -> t.getEstado() == EstadoToma.ADMINISTRADO).count();
        long pendientes = tomasHoy.stream()
                .filter(t -> t.getEstado() == EstadoToma.PENDIENTE || t.getEstado() == EstadoToma.RETRASADO)
                .count();

        model.addAttribute("activeMenu", "mi-plan");
        model.addAttribute("breadcrumb", "Mi Plan de Hoy");
        model.addAttribute("paciente", paciente);
        model.addAttribute("tomasHoy", tomasHoy);
        model.addAttribute("porcentajeAdherencia", porcentaje);
        model.addAttribute("circunferencia", String.format(Locale.US, "%.2f", CIRCUNFERENCIA_ANILLO));
        model.addAttribute("dashOffset", String.format(Locale.US, "%.2f", dashOffset));
        model.addAttribute("administradas", administradas);
        model.addAttribute("pendientes", pendientes);
        return "paciente/mi-plan";
    }

    @PostMapping("/tomas/{id}/verificar")
    public String confirmarMiToma(@PathVariable Long id,
                                  @ModelAttribute TomaVerificacionDto dto,
                                  @AuthenticationPrincipal CustomUserDetails usuario,
                                  RedirectAttributes redirectAttributes) {
        try {
            RegistroToma toma = registroTomaService.verificarYRegistrarToma(id, dto, usuario);
            redirectAttributes.addFlashAttribute("exito",
                    "Confirmaste tu toma de " + toma.getMedicamento().getNombre() + ".");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/paciente/mi-plan";
    }

    @GetMapping("/mis-tratamientos")
    public String misTratamientos(@AuthenticationPrincipal CustomUserDetails usuario, Model model) {
        Paciente paciente = pacienteService.obtenerExpedientePropio(usuario.getId());
        List<Medicamento> medicamentos = medicamentoService.listarActivosPorPaciente(paciente.getId());

        model.addAttribute("activeMenu", "mis-tratamientos");
        model.addAttribute("breadcrumb", "Mis Tratamientos");
        model.addAttribute("paciente", paciente);
        model.addAttribute("medicamentos", medicamentos);
        return "paciente/mis-tratamientos";
    }

    @GetMapping("/mi-perfil")
    public String miPerfil(@AuthenticationPrincipal CustomUserDetails usuario, Model model) {
        Paciente paciente = pacienteService.obtenerExpedientePropio(usuario.getId());
        List<CuidadorPaciente> cuidadores = pacienteService.listarCuidadoresAutorizados(paciente.getId());

        if (!model.containsAttribute("pacienteDto")) {
            PacienteDto dto = PacienteDto.builder()
                    .id(paciente.getId())
                    .nombreCompleto(paciente.getNombreCompleto())
                    .fechaNacimiento(paciente.getFechaNacimiento())
                    .alergias(paciente.getAlergias())
                    .notasClinicas(paciente.getNotasClinicas())
                    .build();
            model.addAttribute("pacienteDto", dto);
        }

        model.addAttribute("activeMenu", "mi-perfil");
        model.addAttribute("breadcrumb", "Mi Perfil Clínico");
        model.addAttribute("paciente", paciente);
        model.addAttribute("cuidadores", cuidadores);
        return "paciente/mi-perfil";
    }

    @PostMapping("/mi-perfil")
    public String actualizarPerfil(@Valid @ModelAttribute("pacienteDto") PacienteDto dto,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal CustomUserDetails usuario,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/paciente/mi-perfil";
        }
        pacienteService.actualizarPerfilPropio(usuario.getId(), dto);
        redirectAttributes.addFlashAttribute("exito", "Tu información clínica y alergias fueron actualizadas.");
        return "redirect:/paciente/mi-perfil";
    }
}