package com.example.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@lombok.Builder
public class EstoqueRequest {

    @NotBlank
    private String nomeEquipamento;

    private String tagPatrimonio;

    private BigDecimal valorUnitario;

    private BigDecimal valorLocacao;

    private String fotoBase64;

    private String fotoBase64Secundaria;

    private Boolean atualizarFotos;

    @NotNull
    private Long empresaId;

    private Long canteiroId;

    private Long equipeResponsavelId;

    // Novo campo para relacionamento direto com equipe
    private Long equipeId;
}
