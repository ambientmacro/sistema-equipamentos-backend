package com.example.controller;

import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.dto.ExecucaoPainelDTO;
import com.example.dto.ExecucaoRequest;
import com.example.dto.ExecucaoResumoDTO;
import com.example.entity.Execucao;
import com.example.service.ExecucaoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/execucoes")
@RequiredArgsConstructor
public class ExecucaoController {

    private final ExecucaoService service;

    // ✅ CREATE
    @PostMapping
    public Execucao criar(@Validated @RequestBody ExecucaoRequest request) {
        return service.salvar(request);
    }

    // ✅ LISTAR PAGINADO (remove o antigo)
    @GetMapping
    public Page<Execucao> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.listar(page, size);
    }

    // ✅ RESUMO PAGINADO (único agora)
    @GetMapping("/resumo")
    public Page<ExecucaoResumoDTO> listarResumo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.listarResumoPaginado(page, size);
    }

    // ✅ BUSCAR POR ID
    @GetMapping("/{id}")
    public Execucao buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // ✅ EQUIPE PAGINADO
    @GetMapping("/equipe/{equipeId}")
    public Page<Execucao> listarPorEquipe(
            @PathVariable Long equipeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.listarPorEquipe(equipeId, page, size);
    }

    // ✅ ESTOQUE PAGINADO
    @GetMapping("/estoque/{estoqueId}")
    public Page<Execucao> listarPorEstoque(
            @PathVariable Long estoqueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.listarPorEstoque(estoqueId, page, size);
    }

    // ✅ PAINEL (mantido)
    @GetMapping("/estoque/{estoqueId}/semana-atual")
    public java.util.List<ExecucaoPainelDTO> listarSemanaAtualPorEstoque(
            @PathVariable Long estoqueId) {

        return service.listarSemanaAtualPorEstoque(estoqueId);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}