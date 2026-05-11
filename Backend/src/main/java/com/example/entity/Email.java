package com.example.entity;

import com.example.enums.EmailTipo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
@Table(name = "email")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "nome")
    private String nome;

    @Column(name = "setor")
    private String setor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    @Builder.Default
    private EmailTipo tipo = EmailTipo.DESTINATARIO;

    @PrePersist
    public void prePersist() {
        if (tipo == null) {
            tipo = EmailTipo.DESTINATARIO;
        }
    }
}
