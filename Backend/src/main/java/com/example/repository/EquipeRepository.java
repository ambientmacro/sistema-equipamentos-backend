package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Equipe;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    Optional<Equipe> findByNome(String nome);

}