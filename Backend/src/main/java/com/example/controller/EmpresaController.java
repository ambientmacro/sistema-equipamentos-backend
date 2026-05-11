package com.example.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.EmpresaRequest;
import com.example.entity.Empresa;
import com.example.service.EmpresaService;

@RestController
@RequestMapping("/api/v1/empresas")

public class EmpresaController {

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    
    @PostMapping
    public Empresa criar(
            @Validated @RequestBody EmpresaRequest request,
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {
        return service.salvar(request, actorTipo);
    }

    @PutMapping("/{id}")
    public Empresa atualizar(
            @PathVariable Long id,
            @Validated @RequestBody EmpresaRequest request,
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {
        return service.atualizar(id, request, actorTipo);
    }

    
    
    @GetMapping
    public List<Empresa> listar() {
        return service.listar();
    }

    
    @GetMapping("/{id}")
    public Empresa buscarPorId(@PathVariable Long id) {
        return service.getEmpresaById(id);
    }

    
    @DeleteMapping("/{id}")
    public void deletar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {
        service.deletar(id, actorTipo);
    }
}