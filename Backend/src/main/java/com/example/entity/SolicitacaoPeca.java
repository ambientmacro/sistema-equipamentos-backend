package com.example.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.enums.SolicitacaoPecaStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitacao_peca")
public class SolicitacaoPeca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Estoque equipamento;

    @ManyToOne
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    @ManyToOne
    @JoinColumn(name = "email_remetente_id")
    private EmailRemetente emailRemetente;

    @CreationTimestamp
    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(length = 255)
    private String assunto;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 255)
    private String anexoNome;

    @Column(length = 100)
    private String anexoTipo;

    @Column(columnDefinition = "TEXT")
    private String anexoBase64;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SolicitacaoPecaStatus status;
}
