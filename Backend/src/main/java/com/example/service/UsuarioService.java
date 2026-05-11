package com.example.service;

import java.util.Locale;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.dto.UsuarioRequest;
import com.example.dto.UsuarioResponse;
import com.example.entity.Equipe;
import com.example.entity.TipoCategoria;
import com.example.entity.Usuario;
import com.example.exception.BusinessException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EquipeRepository;
import com.example.repository.TipoCategoriaRepository;
import com.example.repository.UsuarioRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EquipeRepository equipeRepository;
    private final TipoCategoriaRepository tipoCategoriaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse getById(Long id) {
        return usuarioRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    public void delete(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        usuarioRepository.delete(usuario);
    }

    // ✅ REFATORADO (PAGINAÇÃO + CACHE + CIRCUIT BREAKER)
    @Cacheable("usuarios")
    @CircuitBreaker(name = "usuarioService", fallbackMethod = "fallbackGetAll")
    public Page<UsuarioResponse> getAll(int page, int size) {

        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return usuarioRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ✅ fallback (evita 502)
    public Page<UsuarioResponse> fallbackGetAll(int page, int size, Exception ex) {
        return Page.empty();
    }

    public UsuarioResponse save(UsuarioRequest request, String actorTipo) {
        String normalizedUsername = normalizeUsername(request.getUsername());
        String normalizedPassword = normalizePasswordRequired(request.getSenha());

        if (usuarioRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new BusinessException("Já existe um usuário com esse username");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException("Email obrigatório");
        }

        TipoCategoria tipoCategoria = getTipoCategoriaObrigatorio(request);
        validateRoleAssignmentPermission(actorTipo, tipoCategoria);

        Equipe equipe = resolveEquipeForCreate(request, tipoCategoria);

        Usuario usuario = Usuario.builder()
                .username(normalizedUsername)
                .senha(passwordEncoder.encode(normalizedPassword))
                .equipe(equipe)
                .nome(request.getNome())       // ✅ ADICIONAR
                .email(request.getEmail())     // ✅ ADICIONAR
                .ativo(true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse update(Long id, UsuarioRequest request, String actorTipo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String normalizedUsername = normalizeUsername(request.getUsername());
        Usuario usuarioExistente = usuarioRepository.findByUsernameIgnoreCase(normalizedUsername).orElse(null);

        if (usuarioExistente != null && !usuarioExistente.getId().equals(usuario.getId())) {
            throw new BusinessException("Já existe um usuário com esse username");
        }

        TipoCategoria tipoCategoria = getTipoCategoriaObrigatorio(request);
        validateRoleAssignmentPermission(actorTipo, tipoCategoria);

        usuario.setUsername(normalizedUsername);
        if (hasPassword(request.getSenha())) {
            usuario.setSenha(passwordEncoder.encode(request.getSenha().trim()));
        }
        usuario.setEquipe(updateEquipeDoUsuario(usuario.getEquipe(), request, tipoCategoria));

        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse inativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        usuario.setAtivo(false);
        return toResponse(usuarioRepository.save(usuario));
    }

    private Equipe resolveEquipeForCreate(UsuarioRequest request, TipoCategoria tipoCategoria) {
        if (request.getEquipeId() != null) {
            Equipe equipe = equipeRepository.findById(request.getEquipeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada"));
            validarTipoCadastro(tipoCategoria, equipe);
            return equipe;
        }

        if (request.getNomeEquipe() == null || request.getNomeEquipe().isBlank()) {
            throw new BusinessException("Equipe obrigatória");
        }

        String nomeEquipe = request.getNomeEquipe().trim();

        if (equipeRepository.findByNome(nomeEquipe).isPresent()) {
            throw new BusinessException("Esta equipe já existe");
        }

        Equipe novaEquipe = new Equipe();
        novaEquipe.setNome(nomeEquipe);
        novaEquipe.setTipoCategoria(tipoCategoria);
        novaEquipe.setAtivo(true);
        return equipeRepository.save(novaEquipe);
    }

    private Equipe updateEquipeDoUsuario(Equipe equipe, UsuarioRequest request, TipoCategoria tipoCategoria) {
        if (request.getEquipeId() != null) {
            Equipe equipeExistente = equipeRepository.findById(request.getEquipeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada"));
            if (equipe != null && equipe.getId() != null && equipe.getId().equals(equipeExistente.getId())) {
                return atualizarEquipeExistente(equipeExistente, request, tipoCategoria);
            }
            validarTipoCadastro(tipoCategoria, equipeExistente);
            return equipeExistente;
        }

        if (equipe == null) {
            return resolveEquipeForCreate(request, tipoCategoria);
        }

        String nomeEquipe = request.getNomeEquipe() == null ? "" : request.getNomeEquipe().trim();
        if (nomeEquipe.isBlank()) {
            throw new BusinessException("Equipe obrigatória");
        }

        Equipe equipeComMesmoNome = equipeRepository.findByNome(nomeEquipe).orElse(null);
        if (equipeComMesmoNome != null && !equipeComMesmoNome.getId().equals(equipe.getId())) {
            throw new BusinessException("Esta equipe já existe");
        }

        equipe.setNome(nomeEquipe);
        equipe.setTipoCategoria(tipoCategoria);
        return equipeRepository.save(equipe);
    }

    private Equipe atualizarEquipeExistente(Equipe equipe, UsuarioRequest request, TipoCategoria tipoCategoria) {
        String nomeEquipe = request.getNomeEquipe() == null ? "" : request.getNomeEquipe().trim();
        if (nomeEquipe.isBlank()) {
            throw new BusinessException("Equipe obrigatória");
        }

        Equipe equipeComMesmoNome = equipeRepository.findByNome(nomeEquipe).orElse(null);
        if (equipeComMesmoNome != null && !equipeComMesmoNome.getId().equals(equipe.getId())) {
            throw new BusinessException("Esta equipe já existe");
        }

        equipe.setNome(nomeEquipe);
        equipe.setTipoCategoria(tipoCategoria);
        return equipeRepository.save(equipe);
    }

    private void validarTipoCadastro(TipoCategoria tipoCategoria, Equipe equipe) {
        if (tipoCategoria == null) {
            return;
        }

        if (equipe.getTipoCategoria() == null) {
            throw new BusinessException("A equipe informada não possui tipo de cadastro vinculado");
        }

        if (!tipoCategoria.getId().equals(equipe.getTipoCategoria().getId())) {
            throw new BusinessException("O tipo de cadastro não corresponde à equipe informada");
        }
    }

    private TipoCategoria getTipoCategoriaObrigatorio(UsuarioRequest request) {
        if (request.getTipoCadastroId() == null) {
            throw new BusinessException("Selecione um tipo de cadastro");
        }

        return tipoCategoriaRepository.findById(request.getTipoCadastroId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de cadastro não encontrado"));
    }

    private void validateRoleAssignmentPermission(String actorTipo, TipoCategoria tipoCategoriaSolicitada) {
        String tipoSolicitado = normalizeRole(tipoCategoriaSolicitada == null ? null : tipoCategoriaSolicitada.getNome());
        String tipoAtor = normalizeRole(actorTipo);

        if ("DEVELOPER".equals(tipoSolicitado) && !"DEVELOPER".equals(tipoAtor)) {
            throw new BusinessException("Apenas DEVELOPER pode definir o tipo de usuário como DEVELOPER");
        }

        if ("GERENCIAL".equals(tipoSolicitado) && !"DEVELOPER".equals(tipoAtor) && !"GERENTE".equals(tipoAtor)) {
            throw new BusinessException("Apenas DEVELOPER e GERENTE podem definir o tipo de usuário como GERENCIAL");
        }
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("Username obrigatório");
        }
        String normalizedUsername = username.trim().toUpperCase(Locale.ROOT);
        if (normalizedUsername.length() != 7) {
            throw new BusinessException("O username deve ter exatamente 7 caracteres");
        }
        return normalizedUsername;
    }

    private String normalizePasswordRequired(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("Senha obrigatória");
        }
        return password.trim();
    }

    private boolean hasPassword(String password) {
        return password != null && !password.isBlank();
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        Long tipoId = usuario.getEquipe() != null && usuario.getEquipe().getTipoCategoria() != null
                ? usuario.getEquipe().getTipoCategoria().getId()
                : null;

        String tipoNome = usuario.getEquipe() != null && usuario.getEquipe().getTipoCategoria() != null
                ? usuario.getEquipe().getTipoCategoria().getNome()
                : null;

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .equipeId(usuario.getEquipe() != null ? usuario.getEquipe().getId() : null)
                .equipe(usuario.getEquipe() != null ? usuario.getEquipe().getNome() : null)
                .tipoCadastroId(tipoId)
                .tipoCadastro(tipoNome)
                .tipoCategoriaId(tipoId)
                .tipoCategoria(tipoNome)
                .ativo(usuario.getAtivo() != null ? usuario.getAtivo() : Boolean.TRUE)
                .tipoUsuario(tipoNome != null ? tipoNome.toUpperCase() : null)
                .build();
    }
}
