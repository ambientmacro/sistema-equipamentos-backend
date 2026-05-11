package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.SolicitacaoPeca;

public interface SolicitacaoPecaRepository extends JpaRepository<SolicitacaoPeca, Long> {

    List<SolicitacaoPeca> findByEquipamentoIdOrderByDataSolicitacaoDesc(Long equipamentoId);

    void deleteByEquipamentoId(Long equipamentoId);
}
