package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.entity.Oficina;

public interface OficinaRepository extends JpaRepository<Oficina, Long> {

    List<Oficina> findByEquipamentoIdOrderByDataDesc(Long equipamentoId);

    void deleteByEquipamentoId(Long equipamentoId);

    boolean existsByEquipamentoId(Long equipamentoId);

    @Query("SELECT DISTINCT o.equipamento.id FROM Oficina o WHERE o.equipamento IS NOT NULL")
    List<Long> findEquipamentoIdsComPassagemNaOficina();
}
