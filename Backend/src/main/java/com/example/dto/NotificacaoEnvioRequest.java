package com.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificacaoEnvioRequest {

    @NotNull
    private Long estoqueId;

    @NotNull
    private Long equipeOrigemId;

    @NotNull
    private Long equipeDestinoId;
}
