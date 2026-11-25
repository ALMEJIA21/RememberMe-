package com.rememberme.app.controller;

import com.rememberme.app.dto.AsignacionCuidadorDto;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/resumen")
    public String resumen(Model model) {
        model.addAttribute("activeMenu", "admin-resumen");
        model.addAttribute("breadcrumb", "Resumen Clínico Global");
        model.addAttribute("metricas", adminService.obtenerMetricasGlobales());
        model.addAttribute("cuidadoresEnAlerta", adminService.listarCuidadoresConAlertaDesgaste());
        model.addAttribute("usuarios", adminService.listarDirectorioUsuarios());
        return "admin/resumen";
    }

    @GetMapping("/asignaciones")
    public String asignaciones(Model model) {
        if (!model.containsAttribute("asignacionDto")) {
            model.addAttribute("asignacionDto", new AsignacionCuidadorDto());
        }

        model.addAttribute("activeMenu", "admin-asignaciones");
        model.addAttribute("breadcrumb", "Asignaciones Cuidador–Paciente");
        model.addAttribute("asignaciones", adminService.listarAsignaciones());
        model.addAttribute("cuidadores", adminService.listarCuidadores());
        model.addAttribute("pacientes", adminService.listarPacientesActivos());
        return "admin/asignaciones";
    }

    @PostMapping("/asignaciones")
    public String vincular(@Valid @ModelAttribute("asignacionDto") AsignacionCuidadorDto dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/asignaciones";
        }
        try {
            adminService.vincularCuidadorPaciente(dto);
            redirectAttributes.addFlashAttribute("exito", "Asignación cuidador–paciente guardada correctamente.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/asignaciones";
    }

    @PostMapping("/asignaciones/tipo")
    public String cambiarTipo(@RequestParam Long cuidadorId,
                              @RequestParam Long pacienteId,
                              @RequestParam boolean esPrincipal,
                              RedirectAttributes redirectAttributes) {
        adminService.cambiarRolPrincipal(cuidadorId, pacienteId, esPrincipal);
        redirectAttributes.addFlashAttribute("exito", "Tipo de vínculo actualizado correctamente.");
        return "redirect:/admin/asignaciones";
    }

    @PostMapping("/asignaciones/revocar")
    public String revocar(@RequestParam Long cuidadorId,
                          @RequestParam Long pacienteId,
                          RedirectAttributes redirectAttributes) {
        adminService.revocarVinculo(cuidadorId, pacienteId);
        redirectAttributes.addFlashAttribute("exito", "Vínculo revocado correctamente.");
        return "redirect:/admin/asignaciones";
    }
}