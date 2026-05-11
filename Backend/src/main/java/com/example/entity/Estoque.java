package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.dto.ChecklistModeloVinculoDTO;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estoque")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_equipamento", nullable = false)
    private String nomeEquipamento;

    @Column(name = "tag_patrimonio")
    private String tagPatrimonio;

    @Column(name = "valor_locacao", nullable = false)
    private BigDecimal valorLocacao;

    @Column(name = "valor_unitario", nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "foto_base64", columnDefinition = "TEXT")
    private String fotoBase64;

    @Column(name = "foto_base64_2", columnDefinition = "TEXT")
    private String fotoBase64Secundaria;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "canteiro_id")
    private Canteiro canteiro;

    @ManyToOne
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    @ManyToOne
    @JoinColumn(name = "equipe_responsavel_id")
    private Equipe equipeResponsavel;

    @Default
    @Column(nullable = false)
    private Boolean ativo = true;

    // ✅ 🔥 CAMPO NOVO (ESSENCIAL PARA SYNC INCREMENTAL)
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Transient
    private List<ChecklistModeloVinculoDTO> checklistModelosVinculados;

    // ✅ GARANTE VALORES PADRÃO
    @PrePersist
    public void prePersist() {
        if (ativo == null) {
            ativo = true;
        }

        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
}