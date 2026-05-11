package com.example.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChecklistModeloRequest {

    private String nome;

    private List<Long> equipamentoIds;
}
