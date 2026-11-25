package com.rememberme.app.controller;

import com.rememberme.app.dto.BienestarDto;
import com.rememberme.app.dto.MedicamentoDto;
import com.rememberme.app.dto.PacienteDto;
import com.rememberme.app.dto.TomaVerificacionDto;
import com.rememberme.app.entity.CuidadorPaciente;
import com.rememberme.app.entity.Medicamento;
import com.rememberme.app.entity.RegistroToma;
import com.rememberme.app.entity.enums.EstadoToma;
import com.rememberme.app.entity.enums.ViaAdministracion;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.security.CustomUserDetails;
import com.rememberme.app.service.BienestarService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/cuidador")
@RequiredArgsConstructor
public class CuidadorController {

    private static final double CIRCUNFERENCIA_ANILLO = 2 * Math.PI * 28.0; // r = 28

    private final RegistroTomaService registroTomaService;
    private final PacienteService pacienteService;
    private final MedicamentoService medicamentoService;
    private final BienestarService bienestarService;

    @GetMapping("/panel")
    public String panel(@AuthenticationPrincipal CustomUserDetails usuario, Model model) {
        List<RegistroToma> tomasHoy = registroTomaService.obtenerAgendaHoyCuidador(usuario.getId());
        List<CuidadorPaciente> vinculos = pacienteService.listarVinculosPorCuidador(usuario.getId());
        boolean alertaAutocuidado = bienestarService.tieneAlertaDesgasteConsecutivo(usuario.getId());

        int porcentaje = registroTomaService.calcularPorcentajeAdherencia(tomasHoy);
        double dashOffset = CIRCUNFERENCIA_ANILLO * (1.0 - (porcentaje / 100.0));

        long administradas = tomasHoy.stream().filter(t -> t.getEstado() == EstadoToma.ADMINISTRADO).count();
        long pendientes = tomasHoy.stream()
                .filter(t -> t.getEstado() == EstadoToma.PENDIENTE || t.getEstado() == EstadoToma.RETRASADO)
                .count();
        long omitidas = tomasHoy.stream().filter(t -> t.getEstado() == EstadoToma.OMITIDO).count();

        model.addAttribute("activeMenu", "panel");
        model.addAttribute("breadcrumb", "Panel de Turno");
        model.addAttribute("tomasHoy", tomasHoy);
        model.addAttribute("vinculos", vinculos);
        model.addAttribute("alertaAutocuidado", alertaAutocuidado);
        model.addAttribute("porcentajeAdherencia", porcentaje);
        model.addAttribute("circunferencia", String.format(java.util.Locale.US, "%.2f", CIRCUNFERENCIA_ANILLO));
        model.addAttribute("dashOffset", String.format(java.util.Locale.US, "%.2f", dashOffset));
        model.addAttribute("kpiAdministradas", administradas);
        model.addAttribute("kpiPendientes", pendientes);
        model.addAttribute("kpiOmitidas", omitidas);
        model.addAttribute("kpiPacientes", vinculos.size());
        model.addAttribute("ahora", LocalDateTime.now());
        return "cuidador/panel";
    }

    @PostMapping("/tomas/{id}/verificar")
    public String verificarToma(@PathVariable Long id,
                                @ModelAttribute TomaVerificacionDto dto,
                                @AuthenticationPrincipal CustomUserDetails usuario,
                                RedirectAttributes redirectAttributes) {
        try {
            RegistroToma actualizada = registroTomaService.verificarYRegistrarToma(id, dto, usuario);
            redirectAttributes.addFlashAttribute("exito",
                    "Toma de " + actualizada.getMedicamento().getNombre()
                            + " registrada como " + actualizada.getEstado().getEtiqueta() + ".");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cuidador/panel";
    }

    @GetMapping("/pacientes")
    public String pacientes(@AuthenticationPrincipal CustomUserDetails usuario, Model model) {
        List<CuidadorPaciente> vinculos = pacienteService.listarVinculosPorCuidador(usuario.getId());
        List<Medicamento> medicamentos = medicamentoService.listarActivosPorCuidador(usuario.getId());

        if (!model.containsAttribute("pacienteDto")) {
            model.addAttribute("pacienteDto", new PacienteDto());
        }
        if (!model.containsAttribute("medicamentoDto")) {
            MedicamentoDto medDto = new MedicamentoDto();
            medDto.setFechaHoraInicio(LocalDateTime.now().withSecond(0).withNano(0));
            medDto.setFrecuenciaHoras(12);
            model.addAttribute("medicamentoDto", medDto);
        }

        model.addAttribute("activeMenu", "pacientes");
        model.addAttribute("breadcrumb", "Pacientes y Tratamientos");
        model.addAttribute("vinculos", vinculos);
        model.addAttribute("medicamentos", medicamentos);
        model.addAttribute("vias", ViaAdministracion.values());
        return "cuidador/pacientes";
    }

    @PostMapping("/pacientes")
    public String crearPaciente(@Valid @ModelAttribute("pacienteDto") PacienteDto dto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails usuario,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/cuidador/pacientes";
        }
        pacienteService.crearPacienteParaCuidador(dto, usuario.getId());
        redirectAttributes.addFlashAttribute("exito", "Paciente agregado y vinculado correctamente a tu turno.");
        return "redirect:/cuidador/pacientes";
    }

    @PostMapping("/pacientes/{id}/desactivar")
    public String desactivarPaciente(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails usuario,
                                     RedirectAttributes redirectAttributes) {
        pacienteService.desactivarPacienteParaCuidador(id, usuario.getId());
        redirectAttributes.addFlashAttribute("exito", "Paciente archivado correctamente (borrado lógico seguro).");
        return "redirect:/cuidador/pacientes";
    }

    @PostMapping("/medicamentos")
    public String crearMedicamento(@Valid @ModelAttribute("medicamentoDto") MedicamentoDto dto,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal CustomUserDetails usuario,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/cuidador/pacientes";
        }
        try {
            Medicamento creado = medicamentoService.crearMedicamento(dto, usuario.getId());
            redirectAttributes.addFlashAttribute("exito",
                    "Medicamento " + creado.getNombre() + " añadido al plan terapéutico.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cuidador/pacientes";
    }

    @PostMapping("/medicamentos/{id}/desactivar")
    public String desactivarMedicamento(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails usuario,
                                        RedirectAttributes redirectAttributes) {
        medicamentoService.desactivarMedicamento(id, usuario.getId());
        redirectAttributes.addFlashAttribute("exito", "Medicamento suspendido del plan activo.");
        return "redirect:/cuidador/pacientes";
    }

    @GetMapping("/autocuidado")
    public String autocuidado(@AuthenticationPrincipal CustomUserDetails usuario, Model model) {
        if (!model.containsAttribute("bienestarDto")) {
            BienestarDto dto = new BienestarDto();
            dto.setFechaRegistro(LocalDate.now());
            dto.setNivelEstres(3);
            dto.setNivelFatiga(3);
            model.addAttribute("bienestarDto", dto);
        }

        model.addAttribute("activeMenu", "autocuidado");
        model.addAttribute("breadcrumb", "Mi Autocuidado");
        model.addAttribute("historial", bienestarService.obtenerHistorialReciente(usuario.getId()));
        model.addAttribute("alertaAutocuidado", bienestarService.tieneAlertaDesgasteConsecutivo(usuario.getId()));
        return "cuidador/autocuidado";
    }

    @PostMapping("/autocuidado")
    public String registrarAutocuidado(@Valid @ModelAttribute("bienestarDto") BienestarDto dto,
                                       BindingResult bindingResult,
                                       @AuthenticationPrincipal CustomUserDetails usuario,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/cuidador/autocuidado";
        }
        bienestarService.registrarCheckIn(usuario.getId(), dto);
        redirectAttributes.addFlashAttribute("exito", "Check-in diario de bienestar registrado correctamente.");
        return "redirect:/cuidador/autocuidado";
    }
}