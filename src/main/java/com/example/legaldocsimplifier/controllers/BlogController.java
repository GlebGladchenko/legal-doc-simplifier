package com.example.legaldocsimplifier.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlogController {

    @GetMapping("/blog/how-to-understand-legal-documents")
    public String showLegalDocBlog() {
        return "blog/understand-legal-documents";
    }

    @GetMapping("/blog")
    public String blogIndex() {
        return "blog/index";
    }

    @GetMapping("/blog/simplify-lease-agreement-ai")
    public String post2() {
        return "blog/simplify-lease-agreement-ai";
    }

    @GetMapping("/blog/ai-to-spot-nda-red-flags")
    public String post3() {
        return "blog/ai-to-spot-nda-red-flags";
    }
}
