package com.rememberme.app.controller;

import com.rememberme.app.dto.RegistroUsuarioDto;
import com.rememberme.app.entity.enums.RolUsuario;
import com.rememberme.app.exception.BusinessRuleException;
import com.rememberme.app.exception.ForbiddenException;
import com.rememberme.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        if (!model.containsAttribute("registroDto")) {
            model.addAttribute("registroDto", new RegistroUsuarioDto());
        }
        model.addAttribute("modo", "login");
        return "auth/auth";
    }

    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        if (!model.containsAttribute("registroDto")) {
            RegistroUsuarioDto dto = new RegistroUsuarioDto();
            dto.setRol(RolUsuario.ROL_CUIDADOR);
            model.addAttribute("registroDto", dto);
        }
        model.addAttribute("modo", "register");
        return "auth/auth";
    }

    @PostMapping("/register")
    public String procesarRegistro(@Valid @ModelAttribute("registroDto") RegistroUsuarioDto dto,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modo", "register");
            return "auth/auth";
        }

        try {
            authService.registrarUsuarioPublico(dto);
            redirectAttributes.addFlashAttribute("exito",
                    "Cuenta creada correctamente. Ahora puedes iniciar sesión con tu correo.");
            return "redirect:/auth/login";
        } catch (BusinessRuleException | ForbiddenException ex) {
            model.addAttribute("modo", "register");
            model.addAttribute("errorRegistro", ex.getMessage());
            return "auth/auth";
        }
    }
}