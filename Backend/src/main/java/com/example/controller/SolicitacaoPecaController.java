package com.example.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.SolicitacaoPecaRequest;
import com.example.entity.SolicitacaoPeca;
import com.example.service.SolicitacaoPecaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/solicitacoes-pecas")
@RequiredArgsConstructor
public class SolicitacaoPecaController {

    private final SolicitacaoPecaService service;

    @PostMapping
    public SolicitacaoPeca criar(@Validated @RequestBody SolicitacaoPecaRequest request) {
        return service.salvar(request);
    }

    @GetMapping
    public List<SolicitacaoPeca> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SolicitacaoPeca buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/equipamento/{equipamentoId}")
    public List<SolicitacaoPeca> listarPorEquipamento(@PathVariable Long equipamentoId) {
        return service.listarPorEquipamento(equipamentoId);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
