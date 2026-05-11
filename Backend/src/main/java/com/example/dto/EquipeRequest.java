package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EquipeRequest {

    @NotBlank
    private String nome;

    private Long tipoCategoriaId;
}
