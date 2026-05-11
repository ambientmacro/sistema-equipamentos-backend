package com.example.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.OficinaRequest;
import com.example.entity.Estoque;
import com.example.entity.Oficina;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EstoqueRepository;
import com.example.repository.OficinaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OficinaService {

    /**
     * Cria um novo registro de oficina para o equipamento fornecido.
     */
    @Transactional
    public void salvarNovoEquipamento(Estoque estoque, String observacao) {
        List<Oficina> registrosExistentes = repository.findByEquipamentoIdOrderByDataDesc(estoque.getId());
        if (!registrosExistentes.isEmpty()) {
            Oficina registroAtual = registrosExistentes.get(0);

            if (observacao != null && !observacao.trim().isEmpty()) {
                registroAtual.setObservacao(observacao.trim());
                repository.save(registroAtual);
            }

            if (registrosExistentes.size() > 1) {
                repository.deleteAll(registrosExistentes.subList(1, registrosExistentes.size()));
            }

            return;
        }

        Oficina oficina = Oficina.builder()
                .equipamento(estoque)
                .observacao(observacao)
                .build();
        repository.save(oficina);
    }

    private final OficinaRepository repository;
    private final EstoqueRepository estoqueRepository;

    public Oficina salvar(OficinaRequest request) {
        Estoque equipamento = estoqueRepository.findById(request.getEquipamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipamento nao encontrado"));

        if (equipamento.getEquipeResponsavel() != null || equipamento.getEquipe() != null) {
            equipamento.setEquipeResponsavel(null);
            equipamento.setEquipe(null);
            estoqueRepository.save(equipamento);
        }

        List<Oficina> registrosExistentes = repository.findByEquipamentoIdOrderByDataDesc(equipamento.getId());
        if (!registrosExistentes.isEmpty()) {
            Oficina registroExistente = registrosExistentes.get(0);

            if (request.getObservacao() != null && !request.getObservacao().trim().isEmpty()) {
                registroExistente.setObservacao(request.getObservacao().trim());
                return repository.save(registroExistente);
            }

            return registroExistente;
        }

        Oficina oficina = Oficina.builder()
                .equipamento(equipamento)
                .observacao(request.getObservacao())
                .build();

        return repository.save(oficina);
    }

    public Oficina buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de canteiro nao encontrado"));
    }

    public List<Oficina> listar() {
        Map<String, Oficina> registrosUnicosPorTag = new LinkedHashMap<>();

        repository.findAll().stream()
                .sorted(Comparator
                        .comparing(Oficina::getData, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Oficina::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .filter((registro) -> registro.getEquipamento() != null)
                .filter((registro) -> !Boolean.FALSE.equals(registro.getEquipamento().getAtivo()))
                .forEach((registro) -> {
                    String tagNormalizada = normalizarTag(registro.getEquipamento().getTagPatrimonio());
                    String chaveUnica = tagNormalizada != null
                            ? "tag:" + tagNormalizada
                            : "fallback-id:" + String.valueOf(registro.getEquipamento().getId());
                    registrosUnicosPorTag.putIfAbsent(chaveUnica, registro);
                });

        return List.copyOf(registrosUnicosPorTag.values());
    }

    public List<Oficina> listarPorEquipamento(Long equipamentoId) {
        return repository.findByEquipamentoIdOrderByDataDesc(equipamentoId).stream()
                .filter((registro) -> registro.getEquipamento() != null)
                .filter((registro) -> !Boolean.FALSE.equals(registro.getEquipamento().getAtivo()))
                .toList();
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Registro de canteiro nao encontrado");
        }
        repository.deleteById(id);
    }

    private String normalizarTag(String tag) {
        if (tag == null) {
            return null;
        }

        String tagLimpa = tag.trim();
        if (tagLimpa.isEmpty()) {
            return null;
        }

        return tagLimpa.toUpperCase();
    }


    // Método direcionarParaEquipe removido. Agora está em EstoqueOficinaFacade.
    @Transactional
    public void deletarPorEquipamentoId(Long equipamentoId) {
        if (equipamentoId == null) {
            return;
        }

        repository.deleteByEquipamentoId(equipamentoId);
    }
}
