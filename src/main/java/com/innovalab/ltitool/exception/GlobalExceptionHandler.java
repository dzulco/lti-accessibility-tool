package com.innovalab.ltitool.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Captura errores específicos (Ej: Recurso no encontrado)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNotFound(NoHandlerFoundException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("status", 404);
        mav.addObject("title", "Página no encontrada");
        mav.addObject("message", "Lo sentimos, la página que buscas no existe o fue movida.");

        // Le indicamos explícitamente el nombre del archivo HTML
        mav.setViewName("error");
        return mav;
    }

    // 2. Captura cualquier otro error en el servidor
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception ex) {
        System.err.println("Error interno detectado: " + ex.getMessage());

        ModelAndView mav = new ModelAndView();
        mav.addObject("status", 500);
        mav.addObject("title", "Error Interno del Servidor");
        mav.addObject("message", ex.getMessage() != null ? ex.getMessage() :"Algo salió mal de nuestro lado. Estamos trabajando para solucionarlo.");

        mav.setViewName("error");
        return mav;
    }
}