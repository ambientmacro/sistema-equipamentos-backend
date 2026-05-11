package com.example.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "relatorio")
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @ManyToOne
    @JoinColumn(name = "checklist_execucao_id")
    private Execucao checklistExecucao;

    @ManyToOne
    @JoinColumn(name = "estoque_id", nullable = false)
    private Estoque estoque;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime data;
}
