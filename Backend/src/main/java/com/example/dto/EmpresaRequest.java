package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaRequest {

    @NotBlank
    @Size(max = 255)
    private String nome;
}
