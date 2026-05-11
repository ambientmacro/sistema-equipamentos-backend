package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.ManutencaoRequest;
import com.example.dto.OficinaRequest;
import com.example.entity.Email;
import com.example.entity.Equipe;
import com.example.entity.Estoque;
import com.example.entity.Manutencao;
import com.example.entity.Usuario;
import com.example.enums.ManutencaoStatus;
import com.example.exception.BusinessException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EmailRepository;
import com.example.repository.EquipeRepository;
import com.example.repository.EstoqueRepository;
import com.example.repository.ManutencaoRepository;
import com.example.repository.OficinaRepository;
import com.example.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManutencaoService {

    private static final String DESTINO_EQUIPE = "EQUIPE";
    private static final String DESTINO_OFICINA = "OFICINA";

    private final ManutencaoRepository repository;
    private final EstoqueRepository estoqueRepository;
    private final EquipeRepository equipeRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailRepository emailRepository;
    private final OficinaRepository oficinaRepository;
    private final OficinaService oficinaService;
    private final ExecucaoService execucaoService;
    private final DirecionamentoHistoricoService direcionamentoHistoricoService;

    @Transactional
    public Manutencao salvar(ManutencaoRequest request) {
        Estoque equipamento = estoqueRepository.findById(request.getEquipamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipamento nao encontrado"));

        validarEquipamentoAtivo(equipamento);
        validarStatusInicial(request.getStatus());
        validarEquipamentoSemPendenciaAberta(equipamento.getId());

        Equipe equipeUltima = obterEquipeAtualDoEquipamento(equipamento);

        limparVinculoEquipe(equipamento);
        oficinaRepository.deleteByEquipamentoId(equipamento.getId());

        Email email = null;
        if (request.getEmailId() != null) {
            email = emailRepository.findById(request.getEmailId())
                    .orElseThrow(() -> new ResourceNotFoundException("Email nao encontrado"));
        }

        Manutencao manutencao = Manutencao.builder()
                .equipamento(equipamento)
                .equipeUltima(equipeUltima)
                .equipeConclusao(null)
                .email(email)
                .status(ManutencaoStatus.PENDENTE)
                .observacao(normalizarTexto(request.getObservacao()))
                .valorUnitarioEquipamento(equipamento.getValorUnitario())
                .build();

        preencherSnapshotEquipamento(manutencao, equipamento);
        direcionamentoHistoricoService.registrarEntradaManutencao(
                equipamento,
                equipeUltima,
                request.getObservacao());

        return repository.save(manutencao);
    }

    @Transactional
    public Manutencao salvarSubmanutencao(Long manutencaoPaiId, ManutencaoRequest request) {
        Manutencao manutencaoPai = repository.findById(manutencaoPaiId)
                .orElseThrow(() -> new ResourceNotFoundException("Manutencao principal nao encontrada"));

        if (manutencaoPai.getManutencaoPai() != null) {
            throw new BusinessException("Nao e permitido criar submanutencao de outra submanutencao.");
        }

        if (manutencaoPai.getStatus() != ManutencaoStatus.PENDENTE) {
            throw new BusinessException("A manutencao principal precisa estar PENDENTE para receber submanutencoes.");
        }

        Estoque equipamento = manutencaoPai.getEquipamento();
        if (equipamento == null) {
            throw new BusinessException("A manutencao principal nao possui equipamento vinculado.");
        }

        validarEquipamentoAtivo(equipamento);
        validarStatusInicial(request.getStatus());
        validarEquipamentoDaSubmanutencao(equipamento.getId(), request.getEquipamentoId());

        Email email = manutencaoPai.getEmail();
        if (request.getEmailId() != null) {
            email = emailRepository.findById(request.getEmailId())
                    .orElseThrow(() -> new ResourceNotFoundException("Email nao encontrado"));
        }

        Manutencao subManutencao = Manutencao.builder()
                .equipamento(equipamento)
                .manutencaoPai(manutencaoPai)
                .equipeUltima(manutencaoPai.getEquipeUltima())
                .equipeConclusao(null)
                .email(email)
                .status(ManutencaoStatus.PENDENTE)
                .observacao(normalizarTexto(request.getObservacao()))
                .descricao(normalizarTexto(request.getDescricao()))
                .fotoNotaFiscal(normalizarTexto(request.getFotoNotaFiscal()))
                .valorTotal(request.getValorTotal())
                .valorUnitarioEquipamento(
                        manutencaoPai.getValorUnitarioEquipamento() != null
                                ? manutencaoPai.getValorUnitarioEquipamento()
                                : equipamento.getValorUnitario())
                .build();

        preencherSnapshotEquipamento(subManutencao, equipamento);

        return repository.save(subManutencao);
    }

    @Transactional
    public Manutencao atualizar(Long id, ManutencaoRequest request, Long actorUserId) {
        Manutencao manutencao = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Manutencao nao encontrada"));

        Estoque equipamento = estoqueRepository.findById(request.getEquipamentoId())
            .orElseThrow(() -> new ResourceNotFoundException("Equipamento nao encontrado"));

        Email email = null;
        if (request.getEmailId() != null) {
            email = emailRepository.findById(request.getEmailId())
                .orElseThrow(() -> new ResourceNotFoundException("Email nao encontrado"));
        }

        manutencao.setEquipamento(equipamento);
        manutencao.setEmail(email);
        manutencao.setObservacao(normalizarTexto(request.getObservacao()));
        preencherSnapshotEquipamento(manutencao, equipamento);

        if (manutencao.getValorUnitarioEquipamento() == null) {
            manutencao.setValorUnitarioEquipamento(equipamento.getValorUnitario());
        }

        if (manutencao.getManutencaoPai() != null) {
            atualizarSubmanutencao(manutencao, request, actorUserId);
        } else if (request.getStatus() == ManutencaoStatus.CONCLUIDO) {
            concluirManutencao(manutencao, request, actorUserId);
        } else if (request.getStatus() == ManutencaoStatus.INUTILIZADO) {
            inutilizarEquipamento(manutencao, request, actorUserId);
        } else {
            manutencao.setStatus(ManutencaoStatus.PENDENTE);
        }

        return repository.save(manutencao);
    }

    private void concluirManutencao(Manutencao manutencao, ManutencaoRequest request, Long actorUserId) {
        Estoque equipamento = manutencao.getEquipamento();
        validarEquipamentoAtivo(equipamento);
        validarSubmanutencoesConcluidas(manutencao.getId());

        manutencao.setStatus(ManutencaoStatus.CONCLUIDO);
        manutencao.setDataSaida(LocalDateTime.now());
        manutencao.setDescricao(normalizarTexto(request.getDescricao()));
        manutencao.setFotoNotaFiscal(normalizarTexto(request.getFotoNotaFiscal()));
        manutencao.setValorTotal(somarValorPrincipalComSubmanutencoes(manutencao, request.getValorTotal()));
        manutencao.setEquipeConclusao(buscarEquipeDoUsuario(actorUserId));

        String destino = normalizarDestino(request.getDestinoAposConclusao());

        if (DESTINO_EQUIPE.equals(destino)) {
            Equipe equipeDestino = buscarEquipeDestinoConclusao(request.getEquipeDestinoId(), manutencao.getEquipeUltima());
            equipamento.setEquipeResponsavel(equipeDestino);
            equipamento.setEquipe(null);
            estoqueRepository.save(equipamento);
            oficinaRepository.deleteByEquipamentoId(equipamento.getId());
            direcionamentoHistoricoService.registrarSaidaManutencaoParaEquipe(
                    equipamento,
                    equipeDestino,
                    request.getDescricao());
            return;
        }

        // Padrao: oficina
        equipamento.setEquipeResponsavel(null);
        equipamento.setEquipe(null);
        estoqueRepository.save(equipamento);

        OficinaRequest oficinaRequest = new OficinaRequest();
        oficinaRequest.setEquipamentoId(equipamento.getId());
        oficinaRequest.setObservacao(normalizarTexto(request.getDescricao()));
        oficinaService.salvar(oficinaRequest);
        direcionamentoHistoricoService.registrarSaidaManutencaoParaCanteiro(
                equipamento,
                request.getDescricao());
    }

    private void inutilizarEquipamento(Manutencao manutencao, ManutencaoRequest request, Long actorUserId) {
        Estoque equipamento = manutencao.getEquipamento();
        validarEquipamentoAtivo(equipamento);
        validarSubmanutencoesConcluidas(manutencao.getId());

        manutencao.setStatus(ManutencaoStatus.INUTILIZADO);
        manutencao.setDataSaida(LocalDateTime.now());
        manutencao.setDescricao(normalizarTexto(request.getDescricao()));
        manutencao.setFotoNotaFiscal(normalizarTexto(request.getFotoNotaFiscal()));
        manutencao.setValorTotal(request.getValorTotal());
        manutencao.setEquipeConclusao(buscarEquipeDoUsuario(actorUserId));

        inativarEquipamentoPorManutencao(equipamento);
        oficinaRepository.deleteByEquipamentoId(equipamento.getId());
    }

    private void atualizarSubmanutencao(Manutencao manutencao, ManutencaoRequest request, Long actorUserId) {
        Estoque equipamento = manutencao.getEquipamento();
        validarEquipamentoAtivo(equipamento);
        ManutencaoStatus statusAtual = manutencao.getStatus();

        manutencao.setDescricao(normalizarTexto(request.getDescricao()));
        manutencao.setFotoNotaFiscal(normalizarTexto(request.getFotoNotaFiscal()));
        manutencao.setValorTotal(request.getValorTotal());

        if (request.getStatus() == ManutencaoStatus.PENDENTE) {
            manutencao.setStatus(ManutencaoStatus.PENDENTE);
            manutencao.setDataSaida(null);
            manutencao.setEquipeConclusao(null);
            return;
        }

        if (request.getStatus() != ManutencaoStatus.CONCLUIDO) {
            throw new BusinessException("Submanutencao aceita apenas status PENDENTE ou CONCLUIDO.");
        }

        manutencao.setStatus(ManutencaoStatus.CONCLUIDO);
        if (statusAtual != ManutencaoStatus.CONCLUIDO || manutencao.getDataSaida() == null) {
            manutencao.setDataSaida(LocalDateTime.now());
        }
        if (manutencao.getEquipeConclusao() == null) {
            manutencao.setEquipeConclusao(buscarEquipeDoUsuario(actorUserId));
        }
    }

    private Equipe buscarEquipeDestinoConclusao(Long equipeDestinoId, Equipe equipeUltima) {
        if (equipeDestinoId != null) {
            Equipe equipe = equipeRepository.findById(equipeDestinoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipe de destino nao encontrada"));

            validarEquipeOperacionalAtiva(equipe);
            return equipe;
        }

        if (equipeUltima != null) {
            validarEquipeOperacionalAtiva(equipeUltima);
            return equipeUltima;
        }

        throw new BusinessException("Selecione uma equipe de destino para concluir a manutencao.");
    }

    private void validarEquipeOperacionalAtiva(Equipe equipe) {
        if (equipe == null) {
            throw new BusinessException("Equipe de destino invalida.");
        }

        if (Boolean.FALSE.equals(equipe.getAtivo())) {
            throw new BusinessException("Equipe de destino inativa.");
        }

        String tipo = equipe.getTipoCategoria() == null ? "" : String.valueOf(equipe.getTipoCategoria().getNome()).trim().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(tipo) || "DEVELOPER".equals(tipo)) {
            throw new BusinessException("A equipe de destino deve ser operacional.");
        }
    }

    private Equipe obterEquipeAtualDoEquipamento(Estoque equipamento) {
        if (equipamento.getEquipeResponsavel() != null) {
            return equipamento.getEquipeResponsavel();
        }
        return equipamento.getEquipe();
    }

    private void validarStatusInicial(ManutencaoStatus status) {
        if (status != ManutencaoStatus.PENDENTE) {
            throw new BusinessException("Novo registro de manutencao deve iniciar como PENDENTE.");
        }
    }

    private void validarEquipamentoSemPendenciaAberta(Long equipamentoId) {
        if (repository.existsByEquipamentoIdAndStatus(equipamentoId, ManutencaoStatus.PENDENTE)) {
            throw new BusinessException("Este equipamento ja esta com manutencao pendente.");
        }
    }

    private void validarEquipamentoDaSubmanutencao(Long equipamentoIdEsperado, Long equipamentoIdInformado) {
        if (equipamentoIdInformado == null || equipamentoIdEsperado.equals(equipamentoIdInformado)) {
            return;
        }

        throw new BusinessException("A submanutencao deve usar o mesmo equipamento da manutencao principal.");
    }

    private void validarSubmanutencoesConcluidas(Long manutencaoId) {
        if (repository.existsByManutencaoPaiIdAndStatus(manutencaoId, ManutencaoStatus.PENDENTE)) {
            throw new BusinessException("Conclua as submanutencoes pendentes antes de finalizar a manutencao principal.");
        }
    }

    private BigDecimal somarValorPrincipalComSubmanutencoes(Manutencao manutencao, BigDecimal valorPrincipal) {
        BigDecimal total = valorPrincipal != null ? valorPrincipal : BigDecimal.ZERO;

        if (manutencao.getSubManutencoes() == null || manutencao.getSubManutencoes().isEmpty()) {
            return total;
        }

        for (Manutencao subManutencao : manutencao.getSubManutencoes()) {
            if (subManutencao == null || subManutencao.getValorTotal() == null) {
                continue;
            }
            total = total.add(subManutencao.getValorTotal());
        }

        return total;
    }

    private String normalizarDestino(String destino) {
        String normalized = destino == null ? "" : destino.trim().toUpperCase(Locale.ROOT);
        if (DESTINO_EQUIPE.equals(normalized)) {
            return DESTINO_EQUIPE;
        }
        return DESTINO_OFICINA;
    }

    private Equipe buscarEquipeDoUsuario(Long actorUserId) {
        if (actorUserId == null) {
            return null;
        }

        Usuario usuario = usuarioRepository.findById(actorUserId).orElse(null);
        if (usuario == null) {
            return null;
        }

        return usuario.getEquipe();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private void inativarEquipamentoPorManutencao(Estoque equipamento) {
        if (Boolean.FALSE.equals(equipamento.getAtivo())) {
            return;
        }

        execucaoService.arquivarRelatoriosEEncerrarChecklistsPorEstoque(equipamento.getId());
        equipamento.setAtivo(false);
        estoqueRepository.save(equipamento);
    }

    private void limparVinculoEquipe(Estoque equipamento) {
        if (equipamento.getEquipeResponsavel() == null && equipamento.getEquipe() == null) {
            return;
        }

        equipamento.setEquipeResponsavel(null);
        equipamento.setEquipe(null);
        estoqueRepository.save(equipamento);
    }

    private void validarEquipamentoAtivo(Estoque equipamento) {
        if (Boolean.FALSE.equals(equipamento.getAtivo())) {
            throw new BusinessException("Equipamento inativo no estoque. Registro mantido apenas para historico.");
        }
    }

    private void preencherSnapshotEquipamento(Manutencao manutencao, Estoque equipamento) {
        if (manutencao == null || equipamento == null) {
            return;
        }

        manutencao.setNomeEquipamentoSnapshot(equipamento.getNomeEquipamento());
        manutencao.setTagPatrimonioSnapshot(equipamento.getTagPatrimonio());
        manutencao.setCanteiroIdSnapshot(equipamento.getCanteiro() != null ? equipamento.getCanteiro().getId() : null);
        manutencao.setCanteiroNomeSnapshot(equipamento.getCanteiro() != null ? equipamento.getCanteiro().getNome() : null);
    }

    public Manutencao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manutencao nao encontrada"));
    }

    public List<Manutencao> listar() {
        return repository.findByManutencaoPaiIsNullOrderByDataEntradaDesc();
    }

    public List<Manutencao> listarPorEquipamento(Long equipamentoId) {
        return repository.findByEquipamentoIdOrderByDataEntradaDesc(equipamentoId);
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Manutencao nao encontrada");
        }
        repository.deleteById(id);
    }
}
