package com.example.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChecklistModeloListagemDTO {
    private Long id;
    private String nome;
    private String arquivoNome;
    private String arquivoOriginalNome;
    private LocalDateTime data;
    private List<ChecklistModeloEquipamentoResumoDTO> equipamentos = new ArrayList<>();

    public ChecklistModeloListagemDTO(
            Long id,
            String nome,
            String arquivoNome,
            String arquivoOriginalNome,
            LocalDateTime data) {
        this.id = id;
        this.nome = nome;
        this.arquivoNome = arquivoNome;
        this.arquivoOriginalNome = arquivoOriginalNome;
        this.data = data;
    }
}
