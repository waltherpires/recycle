package br.com.recycle.backend.controller;
import br.com.recycle.backend.dto.EstoqueResponseDTO;
import br.com.recycle.backend.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Estoque", description = "Endpoints para o gerenciamento de estoques")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @Autowired
    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @Operation(
        summary = "Listar todos os Estoques",
        description = "Retorna uma lista com todos os registros de Estoque disponíveis do usuário autenticado"
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de estoques retornada com sucesso",
            content = @Content(schema = @Schema(implementation = EstoqueResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autorizado - token ausente ou inválido",
            content = @Content
        )
    })
    @GetMapping
    public ResponseEntity<List<EstoqueResponseDTO>> listarTodos(HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("usuarioId");
        List<EstoqueResponseDTO> estoques = estoqueService.listarTodos(usuarioId);
        return ResponseEntity.ok(estoques);
    }

    @Operation(
        summary = "Buscar Estoque por ID",
        description = "Retorna um registro de estoque específico com base no ID informado, pertencente ao usuário autenticado"
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estoque encontrado com sucesso",
            content = @Content(schema = @Schema(implementation = EstoqueResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Estoque não encontrado ou não pertence ao usuário",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autorizado - token ausente ou inválido",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<EstoqueResponseDTO> buscarPorId(
            @Parameter(description = "ID do estoque a ser consultado", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long usuarioId = (Long) request.getAttribute("usuarioId");
            EstoqueResponseDTO estoque = estoqueService.buscarPorId(id, usuarioId);
            return ResponseEntity.ok(estoque);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
