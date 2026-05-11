package com.example.dto;

import com.example.entity.Estoque;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueDTO {

    private Long id;
    private String nomeEquipamento;
    private String tagPatrimonio;

    private String fotoBase64;
    private String fotoBase64Secundaria;

    private boolean emExecucao;

    private CanteiroResumoDTO canteiro;
    private EquipeDTO equipeResponsavel;

    private Long equipeId;

    // ✅ CONSTRUTOR PRINCIPAL
    public EstoqueDTO(Estoque estoque, boolean emExecucao) {

        if (estoque == null) {
            return;
        }

        this.id = estoque.getId();
        this.nomeEquipamento = estoque.getNomeEquipamento();
        this.tagPatrimonio = estoque.getTagPatrimonio();

        this.fotoBase64 = estoque.getFotoBase64();
        this.fotoBase64Secundaria = estoque.getFotoBase64Secundaria();

        this.emExecucao = emExecucao;

        // ✅ CANTEIRO
        if (estoque.getCanteiro() != null) {
            this.canteiro = new CanteiroResumoDTO(
                    estoque.getCanteiro().getId(),
                    estoque.getCanteiro().getNome()
            );
        }

        // ✅ EQUIPE RESPONSÁVEL
        if (estoque.getEquipeResponsavel() != null) {
            this.equipeResponsavel = new EquipeDTO(
                    estoque.getEquipeResponsavel()
            );
        }

        // ✅ EQUIPE ID
        if (estoque.getEquipe() != null) {
            this.equipeId = estoque.getEquipe().getId();
        }
    }

    // ✅ FACTORY METHOD (OPCIONAL, MAS BOM)
    public static EstoqueDTO of(Estoque estoque, boolean emExecucao) {
        return new EstoqueDTO(estoque, emExecucao);
    }
}