package com.example.controller;

import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.dto.UsuarioRequest;
import com.example.dto.UsuarioResponse;
import com.example.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/usuarios") // 🔥 VERSIONAMENTO AQUI
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public UsuarioResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ================= CREATE =================
    @PostMapping
    public UsuarioResponse save(
            @Validated @RequestBody UsuarioRequest request,
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {
        return service.save(request, actorTipo);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public UsuarioResponse update(
            @PathVariable Long id,
            @Validated @RequestBody UsuarioRequest request,
            @RequestHeader(value = "X-User-Tipo", required = false) String actorTipo) {
        return service.update(id, request, actorTipo);
    }

    // ================= INATIVAR =================
    @PatchMapping("/{id}/inativar")
    public UsuarioResponse inativar(@PathVariable Long id) {
        return service.inativar(id);
    }

    // ================= LISTAR PAGINADO =================
    @GetMapping
    public Page<UsuarioResponse> listar(
            @RequestParam int page,
            @RequestParam int size) {
        return service.getAll(page, size);
    }
}
