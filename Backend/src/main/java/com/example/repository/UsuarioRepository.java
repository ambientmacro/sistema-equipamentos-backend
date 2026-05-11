package com.example.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    // ✅ resolve N+1 automaticamente + paginação
    @EntityGraph(attributePaths = {
            "equipe",
            "equipe.tipoCategoria"
    })
    Page<Usuario> findAll(Pageable pageable);
}