package com.example.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.dto.EmpresaRequest;
import com.example.entity.Empresa;
import com.example.exception.BusinessException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EmpresaRepository;
import com.example.repository.EstoqueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository repository;
    private final EstoqueRepository estoqueRepository;

    public Empresa getEmpresaById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
    }

    public Empresa salvar(EmpresaRequest request, String actorTipo) {
        validarPermissaoGestao(actorTipo);
        String nome = normalizarNome(request.getNome());
        validarNomeDuplicado(nome, null);
        return repository.save(Empresa.builder().nome(nome).build());
    }

    public Empresa atualizar(Long id, EmpresaRequest request, String actorTipo) {
        validarPermissaoGestao(actorTipo);
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        String nome = normalizarNome(request.getNome());
        validarNomeDuplicado(nome, id);
        empresa.setNome(nome);
        return repository.save(empresa);
    }

    public void deletar(Long id, String actorTipo) {
        validarPermissaoGestao(actorTipo);

        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        if (estoqueRepository.existsByEmpresaId(empresa.getId())) {
            throw new BusinessException("Não é possível excluir empresa vinculada a equipamentos");
        }

        repository.deleteById(empresa.getId());
    }

    public List<Empresa> listar() {
        return repository.findAll();
    }

    private void validarNomeDuplicado(String nome, Long idAtual) {
        boolean duplicado = idAtual == null
                ? repository.existsByNomeIgnoreCase(nome)
                : repository.existsByNomeIgnoreCaseAndIdNot(nome, idAtual);

        if (duplicado) {
            throw new BusinessException("Já existe uma empresa com esse nome");
        }
    }

    private void validarPermissaoGestao(String actorTipo) {
        String tipo = normalizarTipo(actorTipo);
        if (!"DEVELOPER".equals(tipo) && !"GERENTE".equals(tipo) && !"GERENCIAL".equals(tipo)) {
            throw new BusinessException("Apenas GERENTE e DEVELOPER podem gerenciar empresas");
        }
    }

    private String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("Nome da empresa obrigatório");
        }
        return nome.trim();
    }

    private String normalizarTipo(String tipo) {
        return tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
    }
}
