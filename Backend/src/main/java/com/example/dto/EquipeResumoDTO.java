package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipeResumoDTO {
    private Long id;
    private String nome;
    private TipoCategoriaResumoDTO tipoCategoria;
}
