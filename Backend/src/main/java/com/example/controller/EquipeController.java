package com.example.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.EquipeRequest;
import com.example.entity.Equipe;
import com.example.service.EquipeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService service;

    @PostMapping
    public Equipe criar(@Validated @RequestBody EquipeRequest request) {
        return service.salvar(request);
    }

    @GetMapping
    public List<Equipe> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Equipe buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Equipe atualizar(@PathVariable Long id, @Validated @RequestBody EquipeRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    public Equipe inativar(@PathVariable Long id) {
        return service.inativar(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
