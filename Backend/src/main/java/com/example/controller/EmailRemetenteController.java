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

import com.example.entity.EmailRemetente;
import com.example.service.EmailRemetenteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/emails-remetentes")
@RequiredArgsConstructor
public class EmailRemetenteController {

    private final EmailRemetenteService service;

    @PostMapping
    public EmailRemetente criar(
            @RequestBody EmailRemetente emailRemetente,
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {
        return service.save(emailRemetente, actorTipo);
    }

    @GetMapping
    public List<EmailRemetente> listar() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public EmailRemetente buscarPorId(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.delete(id);
    }
}
