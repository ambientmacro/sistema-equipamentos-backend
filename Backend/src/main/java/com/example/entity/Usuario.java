package com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String senha;

    // ✅ CAMPOS OBRIGATÓRIOS
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    // ✅ 🔥 CORREÇÃO AQUI (LAZY → EAGER)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @Builder.Default
    private Boolean ativo = true;

    @PrePersist
    public void prePersist() {
        if (ativo == null) {
            ativo = true;
        }
    }
}