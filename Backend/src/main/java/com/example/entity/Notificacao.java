package com.example.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.enums.NotificacaoStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estoque_id", nullable = false)
    private Estoque estoque;

    @ManyToOne
    @JoinColumn(name = "equipe_origem_id", nullable = false)
    private Equipe equipeOrigem;

    @ManyToOne
    @JoinColumn(name = "equipe_destino_id", nullable = false)
    private Equipe equipeDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificacaoStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;
}
