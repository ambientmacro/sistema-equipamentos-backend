package com.example.dto;

import com.example.enums.SolicitacaoPecaStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolicitacaoPecaRequest {

    @NotNull
    private Long equipamentoId;

    @NotNull
    private Long emailId;

    private Long emailRemetenteId;

    private String assunto;

    private String descricao;

    private String anexoNome;

    private String anexoTipo;

    private String anexoBase64;

    private SolicitacaoPecaStatus status;
}
