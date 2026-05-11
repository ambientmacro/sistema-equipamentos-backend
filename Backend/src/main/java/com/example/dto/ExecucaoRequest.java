package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecucaoRequest {

    @NotNull
    private Long equipeId;

    @NotNull
    private Long estoqueId;

    @NotNull
    private Long checklistModeloId;

    @NotBlank
    private String respostasJson;
}
