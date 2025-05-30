package br.com.recycle.backend.service;

import br.com.recycle.backend.dto.MaterialRequestDTO;
import br.com.recycle.backend.dto.MaterialResponseDTO;
import br.com.recycle.backend.model.Estoque;
import br.com.recycle.backend.model.Material;
import br.com.recycle.backend.repository.EstoqueRepository;
import br.com.recycle.backend.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final EstoqueRepository estoqueRepository;

    @Autowired
    public MaterialService(MaterialRepository materialRepository, EstoqueRepository estoqueRepository) {
        this.materialRepository = materialRepository;
        this.estoqueRepository = estoqueRepository;
    }

    public MaterialResponseDTO criar(MaterialRequestDTO dto, Long usuarioId) {

        if (materialRepository.existsByNomeAndUsuarioId(dto.getNome(), usuarioId)) {
            throw new RuntimeException("Já existe um material com esse nome");
        }

        Material material = new Material();
        material.setNome(dto.getNome());
        material.setDescricao(dto.getDescricao());
        material.setUnidade(dto.getUnidade());
        material.setUsuarioId(usuarioId);

        Material materialSalvo = materialRepository.save(material);
        return MaterialResponseDTO.fromEntity(materialSalvo);
    }

    public MaterialResponseDTO buscarPorId(Long id, Long usuarioId) {

        Material material = materialRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        return MaterialResponseDTO.fromEntity(material);
    }

    public List<MaterialResponseDTO> listarTodos(Long usuarioId) {

        List<Material> materiais = materialRepository.findAllByUsuarioId(usuarioId);

        return materiais.stream()
                .map(MaterialResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public MaterialResponseDTO atualizar(Long id, MaterialRequestDTO dto, Long usuarioId) {
        Material material = materialRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        if (!material.getNome().equals(dto.getNome()) &&
                materialRepository.existsByNomeAndUsuarioId(dto.getNome(), usuarioId)) {
            throw new RuntimeException("Já existe um material com este nome");
        }

        material.setNome(dto.getNome());
        material.setDescricao(dto.getDescricao());
        material.setUnidade(dto.getUnidade());

        Material materialAtualizado = materialRepository.save(material);
        return MaterialResponseDTO.fromEntity(materialAtualizado);
    }

    public void delete(Long id, Long usuarioId) {
        Material material = materialRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Material não encontrado ou você não tem permissão para excluí-lo"));

        estoqueRepository.findByMaterialIdAndMaterial_UsuarioId(id, usuarioId)
                .ifPresent(estoque -> {
                    if (estoque.getQuantidade() > 0) {
                        throw new IllegalStateException("Não é possível excluir este material pois ainda há " +
                                estoque.getQuantidade() + "kg em estoque. Para excluir o material, primeiro retire todo o estoque através de saídas.");
                    }
                });

        materialRepository.delete(material);
    }
}
