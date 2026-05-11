package com.example.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "checklist_modelo")
public class ChecklistModelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "arquivo_nome")
    private String arquivoNome;

    @Column(name = "arquivo_original_nome")
    private String arquivoOriginalNome;

    @Column(name = "arquivo_caminho")
    private String arquivoCaminho;

    @Lob
    @JsonIgnore
    @Column(name = "arquivo_conteudo")
    private byte[] arquivoConteudo;

    @ManyToMany
    @JoinTable(
            name = "checklist_modelo_equipamento",
            joinColumns = @JoinColumn(name = "checklist_modelo_id"),
            inverseJoinColumns = @JoinColumn(name = "equipamento_id"))
    @Builder.Default
    private List<Estoque> equipamentos = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime data;
}
