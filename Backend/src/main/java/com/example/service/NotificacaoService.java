package com.example.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.NotificacaoAceiteRequest;
import com.example.dto.NotificacaoEnvioRequest;
import com.example.entity.Equipe;
import com.example.entity.Estoque;
import com.example.entity.Notificacao;
import com.example.enums.NotificacaoStatus;
import com.example.exception.BusinessException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EquipeRepository;
import com.example.repository.EstoqueRepository;
import com.example.repository.NotificacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final EstoqueRepository estoqueRepository;
    private final EquipeRepository equipeRepository;
    private final DirecionamentoHistoricoService direcionamentoHistoricoService;

    @Transactional
    public Notificacao enviar(NotificacaoEnvioRequest request) {
        Estoque estoque = estoqueRepository.findById(request.getEstoqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipamento nao encontrado"));

        if (Boolean.FALSE.equals(estoque.getAtivo())) {
            throw new ResourceNotFoundException("Equipamento nao encontrado");
        }

        Equipe equipeOrigem = buscarEquipeAtiva(request.getEquipeOrigemId(), "Equipe de origem nao encontrada");
        Equipe equipeDestino = buscarEquipeAtiva(request.getEquipeDestinoId(), "Equipe de destino nao encontrada");

        validarEquipeOperacional(equipeOrigem, "A equipe de origem nao pode enviar equipamento.");
        validarEquipeOperacional(equipeDestino, "Selecione uma equipe operacional para receber o equipamento.");

        if (Objects.equals(equipeOrigem.getId(), equipeDestino.getId())) {
            throw new BusinessException("A equipe de destino deve ser diferente da equipe de origem.");
        }

        if (estoque.getEquipeResponsavel() == null || !Objects.equals(estoque.getEquipeResponsavel().getId(), equipeOrigem.getId())) {
            throw new BusinessException("Esse equipamento nao pertence mais a equipe de origem.");
        }

        if (notificacaoRepository.existsByEstoqueIdAndStatus(estoque.getId(), NotificacaoStatus.PENDENTE)) {
            throw new BusinessException("Ja existe um envio pendente para este equipamento.");
        }

        Notificacao notificacao = Notificacao.builder()
                .estoque(estoque)
                .equipeOrigem(equipeOrigem)
                .equipeDestino(equipeDestino)
                .status(NotificacaoStatus.PENDENTE)
                .build();

        return notificacaoRepository.save(notificacao);
    }

    public List<Notificacao> listarRecebidas(Long equipeDestinoId, String status) {
        Equipe equipeDestino = buscarEquipeAtiva(equipeDestinoId, "Equipe de destino nao encontrada");
        validarEquipeOperacional(equipeDestino, "Somente equipes operacionais podem receber notificacoes.");

        if (status == null || status.isBlank()) {
            return notificacaoRepository.findByEquipeDestinoIdOrderByDataCriacaoDesc(equipeDestinoId);
        }

        NotificacaoStatus statusFiltro;
        try {
            statusFiltro = NotificacaoStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Status de notificacao invalido.");
        }

        return notificacaoRepository.findByEquipeDestinoIdAndStatusOrderByDataCriacaoDesc(equipeDestinoId, statusFiltro);
    }

    @Transactional
    public Notificacao aceitar(Long notificacaoId, NotificacaoAceiteRequest request) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacao nao encontrada"));

        if (notificacao.getStatus() != NotificacaoStatus.PENDENTE) {
            throw new BusinessException("Este envio ja foi processado.");
        }

        Equipe equipeDestino = buscarEquipeAtiva(request.getEquipeDestinoId(), "Equipe de destino nao encontrada");
        validarEquipeOperacional(equipeDestino, "Somente equipes operacionais podem aceitar envio.");

        if (!Objects.equals(notificacao.getEquipeDestino().getId(), equipeDestino.getId())) {
            throw new BusinessException("Apenas a equipe de destino pode aceitar este envio.");
        }

        Estoque estoque = notificacao.getEstoque();
        if (estoque == null || estoque.getId() == null || Boolean.FALSE.equals(estoque.getAtivo())) {
            throw new ResourceNotFoundException("Equipamento nao encontrado");
        }

        if (estoque.getEquipeResponsavel() == null || !Objects.equals(estoque.getEquipeResponsavel().getId(), notificacao.getEquipeOrigem().getId())) {
            throw new BusinessException("O equipamento mudou de equipe antes da confirmacao deste envio.");
        }

        estoque.setEquipeResponsavel(equipeDestino);
        estoque.setEquipe(equipeDestino);
        estoqueRepository.save(estoque);
        direcionamentoHistoricoService.registrarTransferenciaEntreEquipes(
                estoque,
                notificacao.getEquipeOrigem(),
                equipeDestino,
                "Transferencia aceita");

        notificacao.setStatus(NotificacaoStatus.ACEITO);
        notificacao.setDataResposta(LocalDateTime.now());
        return notificacaoRepository.save(notificacao);
    }

    private Equipe buscarEquipeAtiva(Long equipeId, String mensagemErro) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException(mensagemErro));

        if (Boolean.FALSE.equals(equipe.getAtivo())) {
            throw new BusinessException("Equipe inativa nao pode participar deste envio.");
        }

        return equipe;
    }

    private void validarEquipeOperacional(Equipe equipe, String mensagemErro) {
        String tipo = String.valueOf(
                equipe.getTipoCategoria() != null ? equipe.getTipoCategoria().getNome() : "")
                .trim()
                .toUpperCase(Locale.ROOT);

        if ("ADMIN".equals(tipo) || "DEVELOPER".equals(tipo)) {
            throw new BusinessException(mensagemErro);
        }
    }
}
