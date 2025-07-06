package org.novalegal.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleFileSizeLimit(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "File too large. Max 5MB allowed.");
        return "redirect:/";
    }
}
