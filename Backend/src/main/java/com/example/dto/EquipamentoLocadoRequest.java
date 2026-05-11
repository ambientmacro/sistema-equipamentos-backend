package com.example.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipamentoLocadoRequest {

    @NotBlank
    private String nomeLocado;

    private String contrato;

    private String tag;

    @NotNull
    private Long empresaId;

    @NotNull
    private Integer quantidade;

    private BigDecimal valorLocacao;

    private BigDecimal valorUnitario;

    private String fotoUrl;

    private String fotoUrl2;

    private String status;

    private String obra;

    private Long equipeId;

    private String dataLocacao;

    private String dataSaida;

    private BigDecimal indenizacaoValor;

    private String indenizacaoDescricao;

    @Valid
    private List<PecaLocadaRequest> pecas = new ArrayList<>();
}
