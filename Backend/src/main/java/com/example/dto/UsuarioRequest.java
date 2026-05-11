package com.example.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank
    private String username;

    private String senha;

    private String nome;
    private String email;

    // ✅ ID da equipe (NUMERO)
    @JsonAlias({ "equipeId" })
    private Long equipeId;

    // ✅ Nome da equipe (STRING - fallback)
    @JsonAlias({ "nomeEquipe" })
    private String nomeEquipe;

    // ✅ ID do tipo de cadastro
    @NotNull
    @JsonAlias({ "tipoCategoriaId", "tipoCadastroId", "tipo_cadastro" })
    private Long tipoCadastroId;
}