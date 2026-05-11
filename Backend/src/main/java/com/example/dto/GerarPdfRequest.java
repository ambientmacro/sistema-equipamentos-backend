package com.example.dto;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GerarPdfRequest {

    @NotNull(message = "checklistModeloId e obrigatorio.")
    private Long checklistModeloId;

    private String empresa;

    private String equipamento;

    private String tag;

    private String dataChecklist;

    private Map<String, Object> respostas;
}
