package com.netpoint.main.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class NormalControllerExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public String handle(Exception e) {
        return "error/500";
    }
}
