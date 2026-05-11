package com.example.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.entity.Usuario;
import com.example.exception.BusinessException;
import com.example.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario authenticate(String username, String senha) {

        if (username == null || username.isBlank() ||
                senha == null || senha.isBlank()) {
            throw new BusinessException("DADOS_INVALIDOS");
        }

        String normalizedUsername = username.trim().toUpperCase(Locale.ROOT);

        Usuario user = usuarioRepository.findByUsernameIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new BusinessException("USUARIO_NAO_ENCONTRADO"));

        if (!passwordEncoder.matches(senha, user.getSenha())) {
            throw new BusinessException("SENHA_INVALIDA");
        }

        if (user.getAtivo() == null || !user.getAtivo()) {
            throw new BusinessException("USUARIO_INATIVO");
        }

        if (user.getEquipe() == null ||
                user.getEquipe().getAtivo() == null ||
                !user.getEquipe().getAtivo()) {
            throw new BusinessException("EQUIPE_INVALIDA");
        }

        return user;
    }
}