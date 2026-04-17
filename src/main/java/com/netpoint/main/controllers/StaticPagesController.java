package com.netpoint.main.controllers;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class StaticPagesController {
//    @RequestMapping(value = {"/", "/{path:[^\\.]*}"})

    @GetMapping("/test-500")
    public String test500() {
        return "redirect:/error/500.html";
    }

    @PostConstruct
    public void init() {
        System.out.println("StaticPagesController LOADED");
    }
}
