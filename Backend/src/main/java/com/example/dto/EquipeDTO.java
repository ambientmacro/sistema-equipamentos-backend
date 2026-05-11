package com.example.dto;

import com.example.entity.Equipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipeDTO {
    private Long id;
    private String nome;
    private String tipoCategoria;

    public EquipeDTO(Equipe equipe) {
        this.id = equipe.getId();
        this.nome = equipe.getNome();
        this.tipoCategoria = equipe.getTipoCategoria() != null ? equipe.getTipoCategoria().getNome() : null;
    }
}
