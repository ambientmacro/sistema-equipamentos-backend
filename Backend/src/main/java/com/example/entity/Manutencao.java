package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.example.enums.ManutencaoStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
@Table(name = "manutencao")
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
        @JoinColumn(name = "equipamento_id", nullable = true)
    private Estoque equipamento;

        @Column(name = "nome_equipamento_snapshot", length = 255)
        private String nomeEquipamentoSnapshot;

        @Column(name = "tag_patrimonio_snapshot", length = 100)
        private String tagPatrimonioSnapshot;

        @Column(name = "canteiro_id_snapshot")
        private Long canteiroIdSnapshot;

        @Column(name = "canteiro_nome_snapshot", length = 100)
        private String canteiroNomeSnapshot;

    @ManyToOne
    @JoinColumn(name = "equipe_ultima_id")
    private Equipe equipeUltima;

    @ManyToOne
    @JoinColumn(name = "equipe_conclusao_id")
    private Equipe equipeConclusao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manutencao_pai_id")
    @JsonIgnoreProperties({ "subManutencoes", "manutencaoPai" })
    private Manutencao manutencaoPai;

    @Builder.Default
    @OneToMany(mappedBy = "manutencaoPai", fetch = FetchType.EAGER)
    @OrderBy("dataEntrada DESC")
    @JsonIgnoreProperties({ "manutencaoPai", "subManutencoes" })
    private List<Manutencao> subManutencoes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ManutencaoStatus status;

    @CreationTimestamp
    @Column(name = "data_entrada", nullable = false, updatable = false)
    private LocalDateTime dataEntrada;

    @Column(name = "data_saida")
    private LocalDateTime dataSaida;

    @Column(name = "valor_total", precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_unitario_equipamento", precision = 12, scale = 2)
    private BigDecimal valorUnitarioEquipamento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 500)
    private String observacao;

    @Column(name = "foto_nota_fiscal", columnDefinition = "TEXT")
    private String fotoNotaFiscal;

    @ManyToOne
    @JoinColumn(name = "email_id")
    private Email email;
}
