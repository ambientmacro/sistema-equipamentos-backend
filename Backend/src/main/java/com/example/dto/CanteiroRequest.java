package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CanteiroRequest {

    @NotBlank
    @Size(max = 100)
    private String nome;
}
