package com.example.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.DirecionarEquipeRequest;
import com.example.dto.OficinaRequest;
import com.example.entity.Estoque;
import com.example.entity.Oficina;
import com.example.service.EstoqueOficinaFacade;
import com.example.service.OficinaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/v1/oficinas", "/api/v1/canteiros"})
@RequiredArgsConstructor
public class OficinaController {

    private final OficinaService service;
    private final EstoqueOficinaFacade estoqueOficinaFacade;

    @PostMapping
    public Oficina criar(@Validated @RequestBody OficinaRequest request) {
        return service.salvar(request);
    }

    @GetMapping
    public List<Oficina> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Oficina buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/equipamento/{equipamentoId}")
    public List<Oficina> listarPorEquipamento(@PathVariable Long equipamentoId) {
        return service.listarPorEquipamento(equipamentoId);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

    @PatchMapping("/{id}/direcionar-equipe")
    public Estoque direcionarParaEquipe(@PathVariable Long id, @Validated @RequestBody DirecionarEquipeRequest request) {
        return estoqueOficinaFacade.direcionarParaEquipe(id, request);
    }
}
