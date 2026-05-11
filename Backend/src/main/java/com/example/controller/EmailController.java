package com.example.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Email;
import com.example.service.EmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService service;

    @PostMapping
    public Email criar(
            @RequestBody Email email,
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {
        return service.saveEmail(email, actorTipo);
    }

    @GetMapping
    public List<Email> listar() {
        return service.getAllEmails();
    }

    @GetMapping("/{id}")
    public Email buscarPorId(@PathVariable Long id) {
        return service.getEmailById(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deleteEmail(id);
    }
}
