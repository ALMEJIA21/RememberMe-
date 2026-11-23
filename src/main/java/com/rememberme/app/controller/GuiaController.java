package com.rememberme.app.controller;

import com.rememberme.app.dto.GuiaDto;
import com.rememberme.app.entity.Guia;
import com.rememberme.app.service.GuiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/guias")
@RequiredArgsConstructor
public class GuiaController {

    private final GuiaService guiaService;

    @GetMapping
    public String listarYBuscar(@RequestParam(value = "q", required = false) String termino,
                                @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                                @RequestParam(value = "leer", required = false) Long leerId,
                                Model model) {
        List<Guia> guias = guiaService.buscarGuias(termino, categoriaId);
        Guia guiaSeleccionada = null;

        if (leerId != null) {
            guiaSeleccionada = guiaService.obtenerPorId(leerId);
        } else if (!guias.isEmpty()) {
            guiaSeleccionada = guias.get(0);
        }

        if (!model.containsAttribute("guiaDto")) {
            model.addAttribute("guiaDto", new GuiaDto());
        }

        model.addAttribute("activeMenu", "guias");
        model.addAttribute("breadcrumb", "Guías Clínicas y Protocolos");
        model.addAttribute("categorias", guiaService.listarCategoriasActivas());
        model.addAttribute("guias", guias);
        model.addAttribute("guiaSeleccionada", guiaSeleccionada);
        model.addAttribute("termino", termino);
        model.addAttribute("categoriaIdSeleccionada", categoriaId);
        return "guias/guias";
    }

    @PostMapping("/publicar")
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public String publicarGuia(@Valid @ModelAttribute("guiaDto") GuiaDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/guias";
        }
        Guia nueva = guiaService.publicarGuia(dto);
        redirectAttributes.addFlashAttribute("exito", "Guía '" + nueva.getTitulo() + "' publicada correctamente.");
        return "redirect:/guias?leer=" + nueva.getId();
    }
}