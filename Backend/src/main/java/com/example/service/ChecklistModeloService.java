package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.ChecklistModeloRequest;
import com.example.dto.ChecklistModeloEquipamentoResumoDTO;
import com.example.dto.ChecklistModeloListagemDTO;
import com.example.entity.ChecklistModelo;
import com.example.entity.Estoque;
import com.example.entity.Execucao;
import com.example.exception.BusinessException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.ChecklistModeloRepository;
import com.example.repository.ChecklistModeloListagemProjection;
import com.example.repository.ChecklistModeloVinculoResumoProjection;
import com.example.repository.EstoqueRepository;
import com.example.repository.ExecucaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChecklistModeloService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Logger log = LoggerFactory.getLogger(ChecklistModeloService.class);

    private final ChecklistModeloRepository repository;
    private final EstoqueRepository estoqueRepository;
    private final ExecucaoRepository execucaoRepository;

    @Transactional
    public ChecklistModelo salvar(ChecklistModeloRequest request) {
        validarEquipamentosExistentes(request.getEquipamentoIds());
        desvincularEquipamentosDeOutrosModelos(request.getEquipamentoIds(), null);
        ChecklistModelo checklistModelo = ChecklistModelo.builder()
                .nome(validarNomeObrigatorio(request.getNome()))
                .equipamentos(resolverEquipamentos(request.getEquipamentoIds()))
                .build();

        ChecklistModelo salvo = repository.save(checklistModelo);
        return repository.findById(salvo.getId())
                .map(this::inicializarRelacionamentos)
                .orElseGet(() -> inicializarRelacionamentos(salvo));
    }

        private void validarEquipamentosExistentes(List<Long> equipamentoIds) {
        if (equipamentoIds == null || equipamentoIds.isEmpty()) {
            return;
        }

        List<Long> idsInvalidos = equipamentoIds.stream()
            .distinct()
            .filter((id) -> !estoqueRepository.existsById(id))
            .toList();

        if (!idsInvalidos.isEmpty()) {
            throw new ResourceNotFoundException("Equipamentos nao encontrados: " + idsInvalidos);
        }
    }

    public ChecklistModelo importar(String nome, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Selecione um arquivo de modelo para importar.");
        }

        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            throw new BusinessException("Arquivo de modelo invalido.");
        }

        Path diretorio = obterDiretorioModelos();
        String extensao = obterExtensao(nomeOriginal);
        String arquivoSalvo = gerarNomeArquivo(nomeOriginal, extensao);
        Path destino = diretorio.resolve(arquivoSalvo);
        try (InputStream inputStream = arquivo.getInputStream()) {
            Files.createDirectories(diretorio);
            Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException("Nao foi possivel salvar o arquivo do modelo.");
        }

        ChecklistModelo checklistModelo = ChecklistModelo.builder()
                .nome((nome == null || nome.isBlank()) ? removerExtensao(nomeOriginal) : nome.trim())
                .arquivoNome(arquivoSalvo)
                .arquivoOriginalNome(nomeOriginal)
                .arquivoCaminho(destino.toString())
                .equipamentos(new ArrayList<>())
                .build();

        return repository.save(checklistModelo);
    }

    @Transactional
    public ChecklistModelo atualizarArquivo(Long id, MultipartFile arquivo) {
        ChecklistModelo checklistModelo = buscarPorId(id);

        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Selecione um arquivo de modelo para reanexar.");
        }

        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            throw new BusinessException("Arquivo de modelo invalido.");
        }

        Optional<Path> caminhoAnterior = resolverCaminhoArquivo(checklistModelo);

        Path diretorio = obterDiretorioModelos();
        String extensao = obterExtensao(nomeOriginal);
        String arquivoSalvo = gerarNomeArquivo(nomeOriginal, extensao);
        Path destino = diretorio.resolve(arquivoSalvo);
        try (InputStream inputStream = arquivo.getInputStream()) {
            Files.createDirectories(diretorio);
            Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException("Nao foi possivel salvar o arquivo do modelo.");
        }

        if (caminhoAnterior.isPresent()) {
            try {
                Files.deleteIfExists(caminhoAnterior.get());
            } catch (IOException ex) {
                // Nao interrompe o fluxo se falhar ao apagar arquivo anterior.
            }
        }

        checklistModelo.setArquivoNome(arquivoSalvo);
        checklistModelo.setArquivoOriginalNome(nomeOriginal);
        checklistModelo.setArquivoCaminho(destino.toString());

        return repository.save(checklistModelo);
    }

    @Transactional
    public ChecklistModelo atualizar(Long id, ChecklistModeloRequest request) {
        validarEquipamentosExistentes(request.getEquipamentoIds());
        ChecklistModelo checklistModelo = buscarPorId(id);
        desvincularEquipamentosDeOutrosModelos(request.getEquipamentoIds(), id);

        Set<Long> equipamentosAntes = checklistModelo.getEquipamentos().stream()
                .map(Estoque::getId)
                .collect(Collectors.toSet());
        Set<Long> equipamentosDepois = new HashSet<>(request.getEquipamentoIds() == null ? List.of() : request.getEquipamentoIds());

        if (request.getNome() != null && !request.getNome().isBlank()) {
            checklistModelo.setNome(request.getNome().trim());
        }
        checklistModelo.setEquipamentos(resolverEquipamentos(request.getEquipamentoIds()));
        ChecklistModelo modeloAtualizado = repository.save(checklistModelo);

        equipamentosAntes.removeAll(equipamentosDepois);
        arquivarRelatoriosECortarVinculo(id, equipamentosAntes);

        return repository.findById(modeloAtualizado.getId())
                .map(this::inicializarRelacionamentos)
                .orElseGet(() -> inicializarRelacionamentos(modeloAtualizado));
    }

    private void desvincularEquipamentosDeOutrosModelos(List<Long> equipamentoIds, Long modeloAtualId) {
        if (equipamentoIds == null || equipamentoIds.isEmpty()) {
            return;
        }

        List<Long> idsSelecionados = equipamentoIds.stream()
                .filter((id) -> id != null)
                .distinct()
                .toList();
        if (idsSelecionados.isEmpty()) {
            return;
        }

        Map<Long, Set<Long>> removidosPorModelo = new HashMap<>();
        for (ChecklistModeloVinculoResumoProjection vinculo : repository.findVinculosResumoByEquipamentoIds(idsSelecionados)) {
            if (vinculo.getModeloId() == null
                    || vinculo.getEquipamentoId() == null
                    || (modeloAtualId != null && modeloAtualId.equals(vinculo.getModeloId()))) {
                continue;
            }

            removidosPorModelo
                    .computeIfAbsent(vinculo.getModeloId(), (chave) -> new HashSet<>())
                    .add(vinculo.getEquipamentoId());
        }

        removidosPorModelo.forEach(this::arquivarRelatoriosECortarVinculo);

        if (modeloAtualId == null) {
            repository.deleteVinculosByEquipamentoIds(idsSelecionados);
            return;
        }

        repository.deleteVinculosByEquipamentoIdsAndModeloIdNot(idsSelecionados, modeloAtualId);
    }

    @Transactional(readOnly = true)
    public ChecklistModelo buscarPorId(Long id) {
        ChecklistModelo modelo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist modelo nao encontrado"));
        return inicializarRelacionamentos(modelo);
    }

    @Transactional(readOnly = true)
    public List<ChecklistModelo> listar() {
        return repository.findAll().stream()
                .map(this::inicializarRelacionamentos)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChecklistModeloListagemDTO> listarResumo() {
        List<ChecklistModeloListagemDTO> modelos = repository.findAllListagem().stream()
                .map(this::mapearListagem)
                .toList();

        java.util.Map<Long, ChecklistModeloListagemDTO> mapa = modelos.stream()
                .collect(Collectors.toMap(ChecklistModeloListagemDTO::getId, (item) -> item));

        for (ChecklistModeloVinculoResumoProjection vinculo : repository.findAllVinculosResumo()) {
            ChecklistModeloListagemDTO modelo = mapa.get(vinculo.getModeloId());
            if (modelo == null) {
                continue;
            }

            modelo.getEquipamentos().add(new ChecklistModeloEquipamentoResumoDTO(
                    vinculo.getEquipamentoId(),
                    vinculo.getEquipamentoNomeEquipamento(),
                    vinculo.getEquipamentoTagPatrimonio()));
        }

        return modelos;
    }

    @Transactional
    public void deletar(Long id) {
        ChecklistModelo checklistModelo = buscarPorId(id);
        List<Execucao> execucoesComModelo = execucaoRepository.findByChecklistModeloId(id);

        Optional<Path> caminhoArquivo = resolverCaminhoArquivo(checklistModelo);
        if (caminhoArquivo.isPresent()) {
            try {
                Files.deleteIfExists(caminhoArquivo.get());
            } catch (IOException ex) {
                throw new BusinessException("Nao foi possivel excluir o arquivo do modelo.");
            }
        }

        for (Execucao execucao : execucoesComModelo) {
            execucao.setChecklistModelo(null);
        }

        if (!execucoesComModelo.isEmpty()) {
            execucaoRepository.saveAll(execucoesComModelo);
        }

        repository.deleteById(id);
    }

    public Resource baixarArquivo(Long id) {
        ChecklistModelo checklistModelo = buscarPorId(id);
        Optional<Path> caminho = resolverCaminhoArquivo(checklistModelo);
        if (caminho.isPresent()) {
            return new FileSystemResource(caminho.get());
        }

        byte[] conteudoArquivo = checklistModelo.getArquivoConteudo();
        if (conteudoArquivo != null && conteudoArquivo.length > 0) {
            return new ByteArrayResource(conteudoArquivo);
        }

        throw new ResourceNotFoundException("Arquivo do modelo nao encontrado");
    }

    @Transactional
    public void preencherConteudoArquivoAusente() {
        if (!Boolean.parseBoolean(System.getenv("CHECKLIST_SYNC_ARQUIVO_CONTEUDO_ON_STARTUP"))) {
            log.info("Sincronizacao de arquivo_conteudo desativada para preservar memoria do backend.");
            return;
        }

        List<ChecklistModelo> modelos = repository.findAll();

        for (ChecklistModelo modelo : modelos) {
            byte[] conteudoAtual = modelo.getArquivoConteudo();
            if (conteudoAtual != null && conteudoAtual.length > 0) {
                continue;
            }

            Optional<Path> caminho = resolverCaminhoArquivo(modelo);
            if (caminho.isEmpty()) {
                continue;
            }

            try {
                byte[] bytes = Files.readAllBytes(caminho.get());
                if (bytes.length == 0) {
                    continue;
                }

                modelo.setArquivoConteudo(bytes);
                repository.save(modelo);
            } catch (IOException ex) {
                log.warn("Nao foi possivel sincronizar arquivo do modelo {}: {}", modelo.getId(), ex.getMessage());
            }
        }
    }

    public String getArquivoOriginalNome(Long id) {
        ChecklistModelo checklistModelo = buscarPorId(id);
        return checklistModelo.getArquivoOriginalNome() != null && !checklistModelo.getArquivoOriginalNome().isBlank()
                ? checklistModelo.getArquivoOriginalNome()
                : checklistModelo.getArquivoNome();
    }

    private void arquivarRelatoriosECortarVinculo(Long modeloId, Set<Long> equipamentoIds) {
        if (modeloId == null || equipamentoIds == null || equipamentoIds.isEmpty()) {
            return;
        }

        List<Execucao> execucoes = execucaoRepository.findByChecklistModeloId(modeloId).stream()
                .filter(execucao -> execucao.getEstoque() != null && equipamentoIds.contains(execucao.getEstoque().getId()))
                .toList();

        for (Execucao execucao : execucoes) {
            execucao.setChecklistModelo(null);
        }

        if (!execucoes.isEmpty()) {
            execucaoRepository.saveAll(execucoes);
        }
    }

    private List<Estoque> resolverEquipamentos(List<Long> equipamentoIds) {
        if (equipamentoIds == null || equipamentoIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = equipamentoIds.stream()
                .filter((equipamentoId) -> equipamentoId != null)
                .distinct()
                .toList();

        Map<Long, Estoque> equipamentosPorId = estoqueRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Estoque::getId, (equipamento) -> equipamento));

        List<Long> idsNaoEncontrados = ids.stream()
                .filter((equipamentoId) -> !equipamentosPorId.containsKey(equipamentoId))
                .toList();
        if (!idsNaoEncontrados.isEmpty()) {
            throw new ResourceNotFoundException("Equipamento nao encontrado: " + idsNaoEncontrados.get(0));
        }

        return ids.stream()
                .map(equipamentosPorId::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ChecklistModelo inicializarRelacionamentos(ChecklistModelo modelo) {
        if (modelo == null) {
            return null;
        }

        List<Estoque> equipamentos = modelo.getEquipamentos();
        if (equipamentos == null) {
            modelo.setEquipamentos(new ArrayList<>());
            return modelo;
        }

        equipamentos.size();
        equipamentos.forEach((equipamento) -> {
            if (equipamento == null) {
                return;
            }

            if (equipamento.getEmpresa() != null) {
                equipamento.getEmpresa().getNome();
            }

            if (equipamento.getEquipe() != null) {
                equipamento.getEquipe().getNome();
                if (equipamento.getEquipe().getTipoCategoria() != null) {
                    equipamento.getEquipe().getTipoCategoria().getNome();
                }
            }

            if (equipamento.getEquipeResponsavel() != null) {
                equipamento.getEquipeResponsavel().getNome();
                if (equipamento.getEquipeResponsavel().getTipoCategoria() != null) {
                    equipamento.getEquipeResponsavel().getTipoCategoria().getNome();
                }
            }
        });

        return modelo;
    }

    private String validarNomeObrigatorio(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("Nome do modelo e obrigatorio.");
        }

        return nome.trim();
    }

    private Path obterDiretorioModelos() {
        String diretorioConfigurado = System.getenv("APP_MODELOS_DIR");
        if (diretorioConfigurado != null && !diretorioConfigurado.isBlank()) {
            return Paths.get(diretorioConfigurado).toAbsolutePath().normalize();
        }

        Path diretorioAtual = Paths.get("").toAbsolutePath().normalize();
        Path diretorioLocal = diretorioAtual.resolve("ModelosChecklist").normalize();

        if (Files.exists(diretorioLocal)) {
            return diretorioLocal;
        }

        Path diretorioPai = diretorioAtual.getParent();
        if (diretorioPai != null) {
            Path diretorioNoPai = diretorioPai.resolve("ModelosChecklist").normalize();
            if (Files.exists(diretorioNoPai)) {
                return diretorioNoPai;
            }
        }

        return diretorioLocal;
    }

    private Optional<Path> resolverCaminhoArquivo(ChecklistModelo checklistModelo) {
        if (checklistModelo.getArquivoNome() != null && !checklistModelo.getArquivoNome().isBlank()) {
            Optional<Path> porNome = encontrarArquivoPorNome(checklistModelo.getArquivoNome());
            if (porNome.isPresent()) {
                return porNome;
            }
        }

        if (checklistModelo.getArquivoOriginalNome() != null && !checklistModelo.getArquivoOriginalNome().isBlank()) {
            Optional<Path> porNomeOriginal = encontrarArquivoPorNome(checklistModelo.getArquivoOriginalNome());
            if (porNomeOriginal.isPresent()) {
                return porNomeOriginal;
            }
        }

        if (checklistModelo.getArquivoCaminho() != null && !checklistModelo.getArquivoCaminho().isBlank()) {
            Path caminhoPorRegistro = Paths.get(checklistModelo.getArquivoCaminho()).toAbsolutePath().normalize();

            if (Files.exists(caminhoPorRegistro)) {
                return Optional.of(caminhoPorRegistro);
            }
        }

        return Optional.empty();
    }

    private Optional<Path> encontrarArquivoPorNome(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            return Optional.empty();
        }

        for (Path diretorio : listarDiretoriosCandidatos()) {
            Path direto = diretorio.resolve(nomeArquivo).normalize();
            if (Files.exists(direto) && Files.isRegularFile(direto)) {
                return Optional.of(direto);
            }

            Optional<Path> encontrado = buscarRecursivoPorNome(diretorio, nomeArquivo);
            if (encontrado.isPresent()) {
                return encontrado;
            }
        }

        return Optional.empty();
    }

    private Optional<Path> buscarRecursivoPorNome(Path diretorioBase, String nomeArquivo) {
        if (diretorioBase == null || !Files.exists(diretorioBase) || !Files.isDirectory(diretorioBase)) {
            return Optional.empty();
        }

        try (Stream<Path> stream = Files.walk(diretorioBase, 3)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase(nomeArquivo))
                    .findFirst();
        } catch (IOException ex) {
            log.warn("Falha ao buscar arquivo {} em {}: {}", nomeArquivo, diretorioBase, ex.getMessage());
            return Optional.empty();
        }
    }

    private List<Path> listarDiretoriosCandidatos() {
        List<Path> diretorios = new ArrayList<>();

        String diretorioConfigurado = System.getenv("APP_MODELOS_DIR");
        if (diretorioConfigurado != null && !diretorioConfigurado.isBlank()) {
            diretorios.add(Paths.get(diretorioConfigurado).toAbsolutePath().normalize());
        }

        Path atual = Paths.get("").toAbsolutePath().normalize();
        diretorios.add(atual.resolve("ModelosChecklist").normalize());

        Path pai = atual.getParent();
        if (pai != null) {
            diretorios.add(pai.resolve("ModelosChecklist").normalize());
        }

        return diretorios.stream().distinct().toList();
    }

    private String gerarNomeArquivo(String nomeOriginal, String extensao) {
        String base = removerExtensao(nomeOriginal)
                .replaceAll("[^a-zA-Z0-9-_]", "_")
                .replaceAll("_+", "_");

        if (base.isBlank()) {
            base = "modelo";
        }

        return base + "_" + FILE_TIMESTAMP.format(LocalDateTime.now()) + "_" + UUID.randomUUID().toString().substring(0, 8) + extensao;
    }

    private String obterExtensao(String nomeOriginal) {
        int indice = nomeOriginal.lastIndexOf('.');
        return indice >= 0 ? nomeOriginal.substring(indice) : "";
    }

    private String removerExtensao(String nomeOriginal) {
        int indice = nomeOriginal.lastIndexOf('.');
        return indice >= 0 ? nomeOriginal.substring(0, indice) : nomeOriginal;
    }

    private ChecklistModeloListagemDTO mapearListagem(ChecklistModeloListagemProjection projection) {
        return new ChecklistModeloListagemDTO(
                projection.getId(),
                projection.getNome(),
                projection.getArquivoNome(),
                projection.getArquivoOriginalNome(),
                projection.getData());
    }
}
