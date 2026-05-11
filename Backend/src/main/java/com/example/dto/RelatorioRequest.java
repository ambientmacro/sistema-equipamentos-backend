package com.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RelatorioRequest {

    @NotNull
    private Long equipeId;

    @NotNull
    private Long estoqueId;

    private Long checklistExecucaoId;
}
