package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.dto.EquipeRequest;
import com.example.entity.Equipe;
import com.example.entity.TipoCategoria;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EquipeRepository;
import com.example.repository.TipoCategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final TipoCategoriaRepository tipoCategoriaRepository;

    public List<Equipe> listar() {
        return equipeRepository.findAll();
    }

    public Equipe buscarPorId(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada"));
    }

    public Equipe salvar(EquipeRequest request) {
        TipoCategoria tipoCategoria = buscarTipoCategoria(request.getTipoCategoriaId());

        Equipe equipe = Equipe.builder()
                .nome(request.getNome())
                .tipoCategoria(tipoCategoria)
                .ativo(true)
                .build();

        return equipeRepository.save(equipe);
    }

    public Equipe atualizar(Long id, EquipeRequest request) {
        Equipe equipe = buscarPorId(id);
        TipoCategoria tipoCategoria = buscarTipoCategoria(request.getTipoCategoriaId());

        equipe.setNome(request.getNome());
        equipe.setTipoCategoria(tipoCategoria);

        return equipeRepository.save(equipe);
    }

    public Equipe inativar(Long id) {
        Equipe equipe = buscarPorId(id);
        equipe.setAtivo(false);
        return equipeRepository.save(equipe);
    }

    public void deletar(Long id) {
        if (!equipeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipe não encontrada");
        }
        equipeRepository.deleteById(id);
    }

    private TipoCategoria buscarTipoCategoria(Long tipoCategoriaId) {
        if (tipoCategoriaId == null) {
            return null;
        }

        return tipoCategoriaRepository.findById(tipoCategoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de categoria não encontrado"));
    }
}
