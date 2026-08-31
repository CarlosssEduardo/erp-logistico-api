package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.FornecedorItemNovo;
import br.com.carloseduardo.logisticasistema.repository.FornecedorItemNovoRepository;
import br.com.carloseduardo.logisticasistema.service.FornecedorItemNovoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario-novos")
public class FornecedorItemNovoController {

    @Autowired
    private FornecedorItemNovoService service;

    @Autowired
    private FornecedorItemNovoRepository repository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPlanilha(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fornecedor") String fornecedor) {
        try {
            service.processarPlanilhaFornecedor(file, fornecedor);
            return ResponseEntity.ok(Map.of("mensagem", "Planilha de novos itens importada com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<FornecedorItemNovo>> listarPendentes(@RequestParam(required = false) String fornecedor) {
        if (fornecedor != null && !fornecedor.isEmpty() && !fornecedor.equals("TODOS")) {
            return ResponseEntity.ok(repository.findByFornecedorAndVinculado(fornecedor.toUpperCase(), false));
        }
        return ResponseEntity.ok(repository.findByVinculado(false));
    }

    @PostMapping("/vincular")
    public ResponseEntity<?> vincular(@RequestBody Map<String, String> payload) {
        String idItem = payload.get("idItem");
        String skuUniversal = payload.get("skuUniversal");
        String nomeUniversal = payload.get("nomeUniversal");

        service.vincularProduto(idItem, skuUniversal, nomeUniversal);
        return ResponseEntity.ok(Map.of("mensagem", "Vinculado com sucesso!"));
    }

    @Autowired
    private br.com.carloseduardo.logisticasistema.repository.ProdutoEstoqueRepository produtoEstoqueRepository;

    @GetMapping("/buscar-produtos")
    public ResponseEntity<List<br.com.carloseduardo.logisticasistema.model.ProdutoEstoque>> buscarProdutos(@RequestParam("termo") String termo) {
        return ResponseEntity.ok(produtoEstoqueRepository.findByItemContainingIgnoreCaseOrSkuContainingIgnoreCase(termo, termo));
    }
}