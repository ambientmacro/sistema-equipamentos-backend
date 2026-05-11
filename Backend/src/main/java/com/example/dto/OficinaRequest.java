package com.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OficinaRequest {

    @NotNull
    private Long equipamentoId;

    private String observacao;
}
