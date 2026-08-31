package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.ProdutoEstoque;
import br.com.carloseduardo.logisticasistema.repository.ProdutoEstoqueRepository;
import br.com.carloseduardo.logisticasistema.service.ProdutoEstoqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estoque")
public class ProdutoEstoqueController {

    @Autowired
    private ProdutoEstoqueService service;

    @Autowired
    private ProdutoEstoqueRepository repository;

    @PostMapping("/upload")
    public ResponseEntity<?> fazerUploadBalanco(@RequestParam("file") MultipartFile file) {
        try {
            service.processarPlanilhaBalanco(file);
            return ResponseEntity.ok(Map.of("mensagem", "Planilha de Estoque processada com sucesso!"));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar planilha: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listarEstoque(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Boolean isRma,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "35") int size) {

        Page<ProdutoEstoque> pagina = service.listarEstoqueFiltrado(busca, categoria, marca, isRma, PageRequest.of(page, size));

        return ResponseEntity.ok(Map.of(
                "content", pagina.getContent(),
                "totalPages", pagina.getTotalPages(),
                "totalElements", pagina.getTotalElements(),
                "number", pagina.getNumber()
        ));
    }

    @GetMapping("/totais")
    public ResponseEntity<Map<String, Object>> buscarTotais(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Boolean isRma) {
        return ResponseEntity.ok(service.calcularTotaisGlobais(busca, categoria, marca, isRma));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<String>> listarCategorias() {
        return ResponseEntity.ok(service.buscarCategoriasUnicas());
    }

    @GetMapping("/marcas")
    public ResponseEntity<List<String>> listarMarcas() {
        return ResponseEntity.ok(service.buscarMarcasUnicas());
    }

    @PostMapping("/manual")
    public ResponseEntity<ProdutoEstoque> cadastrarManual(@RequestBody ProdutoEstoque produto) {
        produto.setClassificado(true);
        return ResponseEntity.ok(repository.save(produto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoEstoque> atualizarManual(@PathVariable String id, @RequestBody ProdutoEstoque produtoAtualizado) {
        return repository.findById(id).map(produto -> {
            produto.setCategoria(produtoAtualizado.getCategoria());
            produto.setSku(produtoAtualizado.getSku());
            produto.setMarca(produtoAtualizado.getMarca());
            produto.setItem(produtoAtualizado.getItem());
            produto.setQuantidade(produtoAtualizado.getQuantidade());
            produto.setValorUnitario(produtoAtualizado.getValorUnitario());
            produto.setValorTotal((produto.getQuantidade() != null ? produto.getQuantidade() : 0) * (produto.getValorUnitario() != null ? produto.getValorUnitario() : 0.0));
            produto.setClassificado(true);
            return ResponseEntity.ok(repository.save(produto));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}