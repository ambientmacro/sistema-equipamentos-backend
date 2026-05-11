package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PecaLocadaRequest {

    @NotBlank
    private String nome;

    @NotNull
    private Integer quantidade;
}
