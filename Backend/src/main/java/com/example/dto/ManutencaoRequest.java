package com.example.dto;

import java.math.BigDecimal;

import com.example.enums.ManutencaoStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManutencaoRequest {

    @NotNull
    private Long equipamentoId;

    private Long emailId;

    @NotNull
    private ManutencaoStatus status;

    private String observacao;

    private String descricao;

    private String fotoNotaFiscal;

    private BigDecimal valorTotal;

    private Long equipeDestinoId;

    // Valores aceitos: EQUIPE ou OFICINA
    private String destinoAposConclusao;
}
