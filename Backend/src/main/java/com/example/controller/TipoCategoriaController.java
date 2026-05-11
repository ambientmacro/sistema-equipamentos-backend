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

import com.example.entity.TipoCategoria;
import com.example.service.TipoCategoriaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tipo-categoria")
@RequiredArgsConstructor
public class TipoCategoriaController {  
    private final TipoCategoriaService service;

    @GetMapping("/{id}")
    public TipoCategoria getTipoCategoriaById(@PathVariable Long id) {
        return service.getTipoCategoriaById(id);
    }

    @PostMapping
    public TipoCategoria salvar(
            @RequestBody TipoCategoria tipoCategoria,
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {
        return service.salvar(tipoCategoria, actorTipo);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

    @GetMapping
    public List<TipoCategoria> listar() {
        return service.listar();
    }
}
