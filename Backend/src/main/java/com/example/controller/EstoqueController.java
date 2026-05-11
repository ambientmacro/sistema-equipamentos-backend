package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.dto.DirecionarEquipeRequest;
import com.example.dto.EstoqueExclusaoLoteRequest;
import com.example.dto.EstoqueListagemDTO;
import com.example.dto.EstoqueRequest;
import com.example.entity.Estoque;
import com.example.service.EstoqueOficinaFacade;
import com.example.service.EstoqueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/estoques")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService service;
    private final EstoqueOficinaFacade estoqueOficinaFacade;

    @PatchMapping("/{id}/mover-para-oficina")
    public Estoque moverParaOficina(@PathVariable Long id) {
        return estoqueOficinaFacade.moverParaOficina(id);
    }

    @PatchMapping("/{id}/mover-para-canteiro")
    public Estoque moverParaCanteiro(@PathVariable Long id) {
        return estoqueOficinaFacade.moverParaOficina(id);
    }

    @PostMapping
    public Estoque criarEstoque(@Validated @RequestBody EstoqueRequest request) {
        Estoque estoque = service.salvar(request);
        estoqueOficinaFacade.registrarNaOficina(estoque);
        return estoque;
    }

    @PutMapping("/{id}")
    public Estoque atualizarEstoque(@PathVariable Long id,
                                    @Validated @RequestBody EstoqueRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/direcionar-equipe")
    public Estoque direcionarEquipe(@PathVariable Long id,
                                    @Validated @RequestBody DirecionarEquipeRequest request) {
        return service.direcionarParaEquipe(id, request.getEquipeId());
    }

    @GetMapping("/{id}")
    public Estoque obterEstoque(@PathVariable Long id) {
        return service.getEstoqueById(id);
    }

    @DeleteMapping("/{id}")
    public void deletarEstoque(@PathVariable Long id) {
        service.deletar(id);
    }

    @DeleteMapping
    public Map<String, Integer> deletarTodosEstoques(
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {

        return service.deletarTodosAtivos(actorTipo);
    }

    @PostMapping("/exclusao-lote")
    public Map<String, Integer> deletarEstoquesFiltrados(
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo,
            @RequestBody EstoqueExclusaoLoteRequest request) {
        return service.deletarTodosAtivos(actorTipo,
                request == null ? null : request.getIds());
    }

    // ✅ NOVO (incremental)
    @GetMapping
    public Page<EstoqueListagemDTO> listarEstoques(
            @RequestParam(required = false) String criadoDepois,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.listarComFiltroPaginado(criadoDepois, page, size);
    }

    // ✅ mantém paginação intacta
    @GetMapping(params = { "page", "size" })
    public Page<EstoqueListagemDTO> listarEstoquesPaginado(
            @RequestParam int page,
            @RequestParam int size) {
        return service.listarPaginado(page, size);
    }

    @GetMapping("/empresa/{empresaId}")
    public List<EstoqueListagemDTO> listarPorEmpresa(@PathVariable Long empresaId) {
        return service.listarPorEmpresa(empresaId);
    }

    @GetMapping(value = "/empresa/{empresaId}", params = { "page", "size" })
    public Page<EstoqueListagemDTO> listarPorEmpresaPaginado(
            @PathVariable Long empresaId,
            @RequestParam int page,
            @RequestParam int size) {
        return service.listarPorEmpresaPaginado(empresaId, page, size);
    }
}