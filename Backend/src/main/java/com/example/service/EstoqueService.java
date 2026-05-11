package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.EstoqueListagemDTO;
import com.example.dto.EstoqueRequest;
import com.example.entity.*;
import com.example.enums.ManutencaoStatus;
import com.example.exception.BusinessException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.*;

@Service
public class EstoqueService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueService.class);

    private final EstoqueRepository repository;
    private final CanteiroRepository canteiroRepository;
    private final EmpresaRepository empresaRepository;
    private final EquipeRepository equipeRepository;
    private final ManutencaoRepository manutencaoRepository;

    private final ConcurrentMap<String, Object> duplicidadeLocks = new ConcurrentHashMap<>();

    public EstoqueService(
            EstoqueRepository repository,
            CanteiroRepository canteiroRepository,
            EmpresaRepository empresaRepository,
            EquipeRepository equipeRepository,
            ManutencaoRepository manutencaoRepository) {

        this.repository = repository;
        this.canteiroRepository = canteiroRepository;
        this.empresaRepository = empresaRepository;
        this.equipeRepository = equipeRepository;
        this.manutencaoRepository = manutencaoRepository;
    }

    // =====================================================
    // ✅ INCREMENTAL + PAGINAÇÃO
    // =====================================================
    public Page<EstoqueListagemDTO> listarComFiltroPaginado(
            String criadoDepois,
            int page,
            int size) {

        try {
            LocalDateTime data = null;

            if (criadoDepois != null && !criadoDepois.isEmpty()) {
                data = LocalDateTime.parse(criadoDepois);
            }

            return repository.findResumoComFiltroPaginado(
                    data,
                    PageRequest.of(page, size)
            );

        } catch (Exception e) {
            log.warn("Erro incremental paginado", e);

            return repository.findResumoComFiltroPaginado(
                    null,
                    PageRequest.of(page, size)
            );
        }
    }

    // =====================================================
    // ✅ PAGINAÇÃO POR EMPRESA (CORRIGIDO ✅)
    // =====================================================
    public Page<EstoqueListagemDTO> listarPorEmpresaPaginado(
            Long empresaId,
            int page,
            int size) {

        return repository.findResumoAtivosByEmpresaId(
                empresaId,
                PageRequest.of(page, size)
        );
    }

    // =====================================================
    // ✅ LISTAGENS
    // =====================================================
    public List<EstoqueListagemDTO> listar() {
        return repository.findResumoAtivos();
    }

    public Page<EstoqueListagemDTO> listarPaginado(int page, int size) {
        return repository.findResumoAtivos(PageRequest.of(page, size));
    }

    public List<EstoqueListagemDTO> listarPorEmpresa(Long empresaId) {
        return repository.findResumoAtivosByEmpresaId(empresaId);
    }

    // =====================================================
    // ✅ BUSCAR
    // =====================================================
    public Estoque getEstoqueById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipamento nao encontrado"));
    }

    // =====================================================
    // ✅ CRIAR
    // =====================================================
    public Estoque salvar(EstoqueRequest request) {

        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));

        Equipe equipe = null;
        if (request.getEquipeId() != null) {
            equipe = equipeRepository.findById(request.getEquipeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipe nao encontrada"));
        }

        Canteiro canteiro = null;
        if (request.getCanteiroId() != null) {
            canteiro = canteiroRepository.findById(request.getCanteiroId())
                    .orElseThrow(() -> new ResourceNotFoundException("Canteiro nao encontrado"));
        }

        Estoque estoque = Estoque.builder()
                .nomeEquipamento(request.getNomeEquipamento())
                .tagPatrimonio(request.getTagPatrimonio())
                .valorUnitario(normalizar(request.getValorUnitario()))
                .valorLocacao(normalizar(request.getValorLocacao()))
                .empresa(empresa)
                .canteiro(canteiro)
                .equipe(equipe)
                .ativo(true)
                .build();

        return repository.save(estoque);
    }

    // =====================================================
    // ✅ ATUALIZAR
    // =====================================================
    public Estoque atualizar(Long id, EstoqueRequest request) {

        Estoque estoque = getEstoqueById(id);

        estoque.setNomeEquipamento(request.getNomeEquipamento());
        estoque.setTagPatrimonio(request.getTagPatrimonio());
        estoque.setValorUnitario(normalizar(request.getValorUnitario()));
        estoque.setValorLocacao(normalizar(request.getValorLocacao()));

        return repository.save(estoque);
    }

    // =====================================================
    // ✅ DELETAR
    // =====================================================
    @Transactional
    public void deletar(Long id) {

        if (manutencaoRepository.existsByEquipamentoIdAndStatus(id, ManutencaoStatus.PENDENTE)) {
            throw new BusinessException("Equipamento com manutenção pendente");
        }

        repository.deleteById(id);
    }

    // =====================================================
    // ✅ DELETAR TODOS
    // =====================================================
    @Transactional
    public Map<String, Integer> deletarTodosAtivos(
            String tipo,
            List<Long> ids) {

        List<Long> idsParaDeletar;

        if (ids != null && !ids.isEmpty()) {
            idsParaDeletar = repository.findIdsAtivosByIds(ids);
        } else {
            idsParaDeletar = repository.findIdsAtivos();
        }

        int excluidos = 0;

        for (Long id : idsParaDeletar) {
            try {
                repository.deleteById(id);
                excluidos++;
            } catch (Exception e) {
                log.warn("Erro ao deletar id {}", id);
            }
        }

        Map<String, Integer> map = new HashMap<>();
        map.put("total", idsParaDeletar.size());
        map.put("excluidos", excluidos);
        map.put("bloqueados", 0);
        map.put("erros", idsParaDeletar.size() - excluidos);

        return map;
    }

    // =====================================================
    // ✅ COMPATIBILIDADE COM ENDPOINT ANTIGO
    // =====================================================
    @Transactional
    public Map<String, Integer> deletarTodosAtivos(String tipo) {
        return deletarTodosAtivos(tipo, null);
    }


    // =====================================================
    // ✅ DIRECIONAMENTO
    // =====================================================
    public Estoque direcionarParaEquipe(Long estoqueId, Long equipeId) {

        Estoque estoque = getEstoqueById(estoqueId);

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe nao encontrada"));

        estoque.setEquipeResponsavel(equipe);

        return repository.save(estoque);
    }

    public Estoque direcionarParaEquipe(Estoque estoque, Long equipeId) {
        return direcionarParaEquipe(estoque.getId(), equipeId);
    }

    // =====================================================
    // ✅ UTILS
    // =====================================================
    private BigDecimal normalizar(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}