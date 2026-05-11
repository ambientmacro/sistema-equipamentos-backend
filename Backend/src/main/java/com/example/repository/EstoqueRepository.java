package com.example.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.dto.EstoqueListagemDTO;
import com.example.entity.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    // =========================================================
    // ✅ INCREMENTAL + PAGINAÇÃO
    // =========================================================
    @Query("""
        SELECT new com.example.dto.EstoqueListagemDTO(
            e.id,
            e.nomeEquipamento,
            e.tagPatrimonio,
            e.valorLocacao,
            e.valorUnitario,
            emp.id,
            emp.nome,
            c.id,
            c.nome,
            eq.id,
            eq.nome,
            eqTipo.id,
            eqTipo.nome,
            eqResp.id,
            eqResp.nome,
            eqRespTipo.id,
            eqRespTipo.nome,
            (CASE WHEN e.fotoBase64 IS NOT NULL AND e.fotoBase64 <> '' THEN 1 ELSE 0 END) +
            (CASE WHEN e.fotoBase64Secundaria IS NOT NULL AND e.fotoBase64Secundaria <> '' THEN 1 ELSE 0 END)
        )
        FROM Estoque e
        JOIN e.empresa emp
        LEFT JOIN e.canteiro c
        LEFT JOIN e.equipe eq
        LEFT JOIN eq.tipoCategoria eqTipo
        LEFT JOIN e.equipeResponsavel eqResp
        LEFT JOIN eqResp.tipoCategoria eqRespTipo
        WHERE (e.ativo IS NULL OR e.ativo = true)
          AND (:data IS NULL OR e.dataCriacao > :data)
        ORDER BY e.dataCriacao DESC
    """)
    Page<EstoqueListagemDTO> findResumoComFiltroPaginado(
            @Param("data") LocalDateTime data,
            Pageable pageable
    );

    // =========================================================
    // ✅ LISTAGEM NORMAL
    // =========================================================
    @Query("""
        SELECT new com.example.dto.EstoqueListagemDTO(
            e.id,
            e.nomeEquipamento,
            e.tagPatrimonio,
            e.valorLocacao,
            e.valorUnitario,
            emp.id,
            emp.nome,
            c.id,
            c.nome,
            eq.id,
            eq.nome,
            eqTipo.id,
            eqTipo.nome,
            eqResp.id,
            eqResp.nome,
            eqRespTipo.id,
            eqRespTipo.nome,
            (CASE WHEN e.fotoBase64 IS NOT NULL AND e.fotoBase64 <> '' THEN 1 ELSE 0 END) +
            (CASE WHEN e.fotoBase64Secundaria IS NOT NULL AND e.fotoBase64Secundaria <> '' THEN 1 ELSE 0 END)
        )
        FROM Estoque e
        JOIN e.empresa emp
        LEFT JOIN e.canteiro c
        LEFT JOIN e.equipe eq
        LEFT JOIN eq.tipoCategoria eqTipo
        LEFT JOIN e.equipeResponsavel eqResp
        LEFT JOIN eqResp.tipoCategoria eqRespTipo
        WHERE (e.ativo IS NULL OR e.ativo = true)
        ORDER BY e.nomeEquipamento ASC
    """)
    List<EstoqueListagemDTO> findResumoAtivos();

    // =========================================================
    // ✅ PAGINAÇÃO NORMAL
    // =========================================================
    @Query(
            value = """
            SELECT new com.example.dto.EstoqueListagemDTO(
                e.id,
                e.nomeEquipamento,
                e.tagPatrimonio,
                e.valorLocacao,
                e.valorUnitario,
                emp.id,
                emp.nome,
                c.id,
                c.nome,
                eq.id,
                eq.nome,
                eqTipo.id,
                eqTipo.nome,
                eqResp.id,
                eqResp.nome,
                eqRespTipo.id,
                eqRespTipo.nome,
                (CASE WHEN e.fotoBase64 IS NOT NULL AND e.fotoBase64 <> '' THEN 1 ELSE 0 END) +
                (CASE WHEN e.fotoBase64Secundaria IS NOT NULL AND e.fotoBase64Secundaria <> '' THEN 1 ELSE 0 END)
            )
            FROM Estoque e
            JOIN e.empresa emp
            LEFT JOIN e.canteiro c
            LEFT JOIN e.equipe eq
            LEFT JOIN eq.tipoCategoria eqTipo
            LEFT JOIN e.equipeResponsavel eqResp
            LEFT JOIN eqResp.tipoCategoria eqRespTipo
            WHERE (e.ativo IS NULL OR e.ativo = true)
            ORDER BY e.nomeEquipamento ASC
        """,
            countQuery = """
            SELECT COUNT(e)
            FROM Estoque e
            WHERE (e.ativo IS NULL OR e.ativo = true)
        """
    )
    Page<EstoqueListagemDTO> findResumoAtivos(Pageable pageable);

    // =========================================================
    // ✅ POR EMPRESA (SEM PAGINAÇÃO)
    // =========================================================
    @Query("""
        SELECT new com.example.dto.EstoqueListagemDTO(
            e.id,
            e.nomeEquipamento,
            e.tagPatrimonio,
            e.valorLocacao,
            e.valorUnitario,
            emp.id,
            emp.nome,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            0
        )
        FROM Estoque e
        JOIN e.empresa emp
        WHERE emp.id = :empresaId
    """)
    List<EstoqueListagemDTO> findResumoAtivosByEmpresaId(
            @Param("empresaId") Long empresaId
    );

    // =========================================================
    // ✅ POR EMPRESA (COM PAGINAÇÃO)
    // =========================================================
    @Query(
            value = """
            SELECT new com.example.dto.EstoqueListagemDTO(
                e.id,
                e.nomeEquipamento,
                e.tagPatrimonio,
                e.valorLocacao,
                e.valorUnitario,
                emp.id,
                emp.nome,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                0
            )
            FROM Estoque e
            JOIN e.empresa emp
            WHERE emp.id = :empresaId
        """,
            countQuery = """
            SELECT COUNT(e)
            FROM Estoque e
            JOIN e.empresa emp
            WHERE emp.id = :empresaId
        """
    )
    Page<EstoqueListagemDTO> findResumoAtivosByEmpresaId(
            @Param("empresaId") Long empresaId,
            Pageable pageable
    );

    // =========================================================
    // ✅ VALIDAÇÃO TAG DUPLICADA
    // =========================================================
    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
        FROM Estoque e
        WHERE e.tagPatrimonio IS NOT NULL
          AND TRIM(e.tagPatrimonio) <> ''
          AND LOWER(TRIM(e.tagPatrimonio)) = LOWER(TRIM(:tag))
          AND (:id IS NULL OR e.id <> :id)
          AND (e.ativo IS NULL OR e.ativo = true)
    """)
    boolean existsOutroEquipamentoComMesmaTag(
            @Param("tag") String tag,
            @Param("id") Long id
    );

    // =========================================================
    // ✅ IDS
    // =========================================================
    @Query("SELECT e.id FROM Estoque e WHERE e.ativo IS NULL OR e.ativo = true")
    List<Long> findIdsAtivos();

    @Query("SELECT e.id FROM Estoque e WHERE (e.ativo IS NULL OR e.ativo = true) AND e.id IN :ids")
    List<Long> findIdsAtivosByIds(@Param("ids") List<Long> ids);

    // =========================================================
    // ✅ ENTITY
    // =========================================================
    @Override
    Optional<Estoque> findById(Long id);

    boolean existsByCanteiroId(Long canteiroId);
    boolean existsByEmpresaId(Long empresaId);
}