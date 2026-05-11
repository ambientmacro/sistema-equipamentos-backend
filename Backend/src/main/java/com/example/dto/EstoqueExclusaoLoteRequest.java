package com.example.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstoqueExclusaoLoteRequest {
    private List<Long> ids;
}
