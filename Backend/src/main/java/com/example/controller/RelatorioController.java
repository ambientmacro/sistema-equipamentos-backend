package com.example.controller;

import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.dto.RelatorioListagemDTO;
import com.example.dto.RelatorioEstoqueResumoDTO;
import com.example.dto.RelatorioRequest;
import com.example.entity.Relatorio;
import com.example.service.RelatorioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService service;

    // ================= CREATE =================

    @PostMapping
    public Relatorio criar(@Validated @RequestBody RelatorioRequest request) {
        return service.salvar(request);
    }

    @PostMapping("/por-execucao/{execucaoId}")
    public Relatorio gerarPorExecucao(@PathVariable Long execucaoId) {
        return service.gerarPorExecucao(execucaoId);
    }

    // ================= LISTAGEM PAGINADA =================

    @GetMapping
    public Page<RelatorioListagemDTO> listar(
            @RequestParam int page,
            @RequestParam int size) {
        return service.listar(page, size);
    }

    @GetMapping("/{id}")
    public Relatorio buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/equipe/{equipeId}")
    public Page<Relatorio> listarPorEquipe(
            @PathVariable Long equipeId,
            @RequestParam int page,
            @RequestParam int size) {
        return service.listarPorEquipe(equipeId, page, size);
    }

    @GetMapping("/estoque/{estoqueId}")
    public Page<RelatorioListagemDTO> listarPorEstoque(
            @PathVariable Long estoqueId,
            @RequestParam int page,
            @RequestParam int size) {
        return service.listarPorEstoque(estoqueId, page, size);
    }

    @GetMapping("/estoque/{estoqueId}/resumo")
    public Page<RelatorioEstoqueResumoDTO> listarResumoPorEstoque(
            @PathVariable Long estoqueId,
            @RequestParam int page,
            @RequestParam int size) {
        return service.listarResumoPorEstoque(estoqueId, page, size);
    }

    // ================= DELETE =================

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}