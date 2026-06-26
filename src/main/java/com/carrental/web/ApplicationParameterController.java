package com.carrental.web;

import com.carrental.model.ApplicationParameter;
import com.carrental.service.ApplicationParameterService;
import com.carrental.web.dto.ApplicationParameterForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/application-parameters")
public class ApplicationParameterController {

    private final ApplicationParameterService applicationParameterService;

    public ApplicationParameterController(ApplicationParameterService applicationParameterService) {
        this.applicationParameterService = applicationParameterService;
    }

    @GetMapping
    public String index(Model model) {
        List<ApplicationParameter> parameters = applicationParameterService.findAllOrdered();
        model.addAttribute("parameters", parameters);
        model.addAttribute("activeNav", "application-parameters");
        model.addAttribute("pageTitle", "Application parameters");
        return "application-parameters/index";
    }

    @GetMapping("/{code}/edit")
    public String edit(@PathVariable String code, Model model) {
        ApplicationParameter parameter = applicationParameterService.getByCode(code);
        model.addAttribute("parameterForm", applicationParameterService.toForm(parameter));
        model.addAttribute("activeNav", "application-parameters");
        model.addAttribute("pageTitle", "Edit parameter");
        return "application-parameters/edit";
    }

    @PostMapping("/{code}")
    public String update(
            @PathVariable String code,
            @Valid @ModelAttribute("parameterForm") ApplicationParameterForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "application-parameters");
            model.addAttribute("pageTitle", "Edit parameter");
            return "application-parameters/edit";
        }
        try {
            applicationParameterService.update(code, form);
            redirectAttributes.addFlashAttribute("successMessage", "Parameter \"" + code + "\" updated.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/application-parameters/" + code + "/edit";
        }
        return "redirect:/application-parameters";
    }
}
