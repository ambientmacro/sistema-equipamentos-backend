package com.example.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.RelatorioEstoqueResumoDTO;
import com.example.dto.RelatorioListagemDTO;
import com.example.dto.RelatorioRequest;
import com.example.entity.Execucao;
import com.example.entity.Relatorio;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EquipeRepository;
import com.example.repository.ExecucaoRelatorioProjection;
import com.example.repository.ExecucaoRepository;
import com.example.repository.EstoqueRepository;
import com.example.repository.RelatorioListagemProjection;
import com.example.repository.RelatorioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final RelatorioRepository repository;
    private final EquipeRepository equipeRepository;
    private final EstoqueRepository estoqueRepository;
    private final ExecucaoRepository execucaoRepository;

    // =============================
    // CREATE
    // =============================
    @Transactional
    public Relatorio salvar(RelatorioRequest request) {

        var equipe = equipeRepository.findById(request.getEquipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe nao encontrada"));

        var estoque = estoqueRepository.findById(request.getEstoqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipamento nao encontrado"));

        Execucao execucao = null;

        if (request.getChecklistExecucaoId() != null) {
            execucao = execucaoRepository.findById(request.getChecklistExecucaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Execucao nao encontrada"));
        }

        Relatorio relatorio = Relatorio.builder()
                .equipe(equipe)
                .estoque(estoque)
                .checklistExecucao(execucao)
                .build();

        return repository.save(relatorio);
    }

    // =============================
    // GERAR
    // =============================
    @Transactional
    public Relatorio gerarPorExecucao(Long execucaoId) {

        if (execucaoId == null) {
            throw new ResourceNotFoundException("Execucao nao encontrada");
        }

        if (repository.existsByChecklistExecucaoId(execucaoId)) {
            return repository.findByChecklistExecucaoId(execucaoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Relatorio nao encontrado"));
        }

        Execucao execucao = execucaoRepository.findById(execucaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Execucao nao encontrada"));

        Relatorio relatorio = Relatorio.builder()
                .equipe(execucao.getEquipe())
                .estoque(execucao.getEstoque())
                .checklistExecucao(execucao)
                .build();

        return repository.save(relatorio);
    }

    // ✅ LEVE (SEM CARREGAR EXECUCAO COMPLETA)
    @Transactional
    public void gerarPorExecucaoSeNaoExistir(Long execucaoId) {

        if (execucaoId == null) return;

        if (repository.existsByChecklistExecucaoId(execucaoId)) return;

        ExecucaoRelatorioProjection dados = execucaoRepository.findDadosRelatorioById(execucaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Execucao nao encontrada"));

        Relatorio relatorio = Relatorio.builder()
                .equipe(dados.getEquipeId() == null ? null : equipeRepository.getReferenceById(dados.getEquipeId()))
                .estoque(dados.getEstoqueId() == null ? null : estoqueRepository.getReferenceById(dados.getEstoqueId()))
                .checklistExecucao(execucaoRepository.getReferenceById(dados.getId()))
                .build();

        repository.save(relatorio);
    }

    // =============================
    // READ
    // =============================
    @Transactional(readOnly = true)
    public Relatorio buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatorio nao encontrado"));
    }

    // ✅ PAGINADO (REMOVE LIST PERIGOSO)
    @Transactional(readOnly = true)
    public Page<RelatorioListagemDTO> listar(int page, int size) {
        return repository.findAllListagem(criarPageRequest(page, size))
                .map(this::mapearListagem);
    }

    // ✅ PAGINADO DIRETO
    public Page<Relatorio> listarPaginado(int page, int size) {
        return repository.findAll(criarPageRequest(page, size));
    }

    // ✅ PAGINADO EQUIPE
    @Transactional(readOnly = true)
    public Page<Relatorio> listarPorEquipe(Long equipeId, int page, int size) {
        return repository.findByEquipeIdOrderByDataDesc(equipeId, criarPageRequest(page, size));
    }

    // ✅ PAGINADO ESTOQUE
    @Transactional(readOnly = true)
    public Page<RelatorioListagemDTO> listarPorEstoque(Long estoqueId, int page, int size) {
        return repository.findListagemByEstoqueId(estoqueId, criarPageRequest(page, size))
                .map(this::mapearListagem);
    }

    @Transactional(readOnly = true)
    public Page<RelatorioEstoqueResumoDTO> listarResumoPorEstoque(Long estoqueId, int page, int size) {
        return repository.findResumoByEstoqueId(estoqueId, criarPageRequest(page, size));
    }

    // =============================
    // DELETE
    // =============================
    @Transactional
    public void deletar(Long id) {
        Relatorio relatorio = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatorio nao encontrado"));
        repository.delete(relatorio);
    }

    // =============================
    // PAGINATION SAFE
    // =============================
    private PageRequest criarPageRequest(int page, int size) {
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), TAMANHO_MAXIMO_PAGINA);
        return PageRequest.of(paginaSegura, tamanhoSeguro);
    }

    // =============================
    // MAPEAMENTO
    // =============================
    private RelatorioListagemDTO mapearListagem(RelatorioListagemProjection p) {

        var equipeTipo = new RelatorioListagemDTO.TipoCategoriaDTO(
                p.getEquipeTipoCategoriaId(),
                p.getEquipeTipoCategoriaNome());

        var equipe = new RelatorioListagemDTO.EquipeDTO(
                p.getEquipeId(),
                p.getEquipeNome(),
                equipeTipo);

        var equipeRespTipo = new RelatorioListagemDTO.TipoCategoriaDTO(
                p.getEstoqueEquipeResponsavelTipoCategoriaId(),
                p.getEstoqueEquipeResponsavelTipoCategoriaNome());

        var equipeResp = new RelatorioListagemDTO.EquipeDTO(
                p.getEstoqueEquipeResponsavelId(),
                p.getEstoqueEquipeResponsavelNome(),
                equipeRespTipo);

        var empresa = new RelatorioListagemDTO.EmpresaDTO(
                p.getEstoqueEmpresaId(),
                p.getEstoqueEmpresaNome());

        var estoque = new RelatorioListagemDTO.EstoqueDTO(
                p.getEstoqueId(),
                p.getEstoqueNomeEquipamento(),
                p.getEstoqueTagPatrimonio(),
                p.getEstoqueAtivo(),
                empresa,
                equipeResp);

        RelatorioListagemDTO.ExecucaoDTO execucao = null;

        if (p.getChecklistExecucaoId() != null) {

            var execTipo = new RelatorioListagemDTO.TipoCategoriaDTO(
                    p.getChecklistExecucaoEquipeTipoCategoriaId(),
                    p.getChecklistExecucaoEquipeTipoCategoriaNome());

            var execEquipe = new RelatorioListagemDTO.EquipeDTO(
                    p.getChecklistExecucaoEquipeId(),
                    p.getChecklistExecucaoEquipeNome(),
                    execTipo);

            var checklist = new RelatorioListagemDTO.ChecklistModeloDTO(
                    p.getChecklistModeloId(),
                    p.getChecklistModeloNome(),
                    p.getChecklistModeloArquivoNome());

            execucao = new RelatorioListagemDTO.ExecucaoDTO(
                    p.getChecklistExecucaoId(),
                    p.getChecklistExecucaoData(),
                    p.getChecklistExecucaoRespostasJson(),
                    execEquipe,
                    estoque,
                    checklist);
        }

        return new RelatorioListagemDTO(
                p.getId(),
                p.getData(),
                equipe,
                execucao,
                estoque);
    }
}