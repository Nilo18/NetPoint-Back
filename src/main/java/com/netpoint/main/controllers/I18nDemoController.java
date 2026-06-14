package com.netpoint.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class I18nDemoController {

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/message")
    public ResponseEntity<?> getMessage(Locale locale) {
        // Spring automatically injects the current request's locale
        String message = messageSource.getMessage("app.welcome", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }
}