package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.ProdutoMesaCompra;
import br.com.carloseduardo.logisticasistema.repository.ProdutoMesaCompraRepository;
import br.com.carloseduardo.logisticasistema.service.ProdutoMesaCompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mesacompras")
public class ProdutoMesaCompraController {

    @Autowired
    private ProdutoMesaCompraService service;

    @Autowired
    private ProdutoMesaCompraRepository repository;

    @PostMapping("/upload/{categoriaAba}")
    public ResponseEntity<?> fazerUploadBase(@PathVariable String categoriaAba, @RequestParam("file") MultipartFile file) {
        try {
            service.processarPlanilhaBase(file, categoriaAba.toUpperCase());
            return ResponseEntity.ok(Map.of("mensagem", "Planilha base importada com sucesso!"));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar base: " + e.getMessage());
        }
    }

    @GetMapping("/{categoriaAba}")
    public ResponseEntity<Page<ProdutoMesaCompra>> listarMesaPorAba(
            @PathVariable String categoriaAba,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        // 🔥 MUDANÇA AQUI: Trocado DESC por ASC para manter a ordem da Planilha intacta!
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));

        if ((marca != null && !marca.isEmpty() && !marca.equals("TODAS")) || (busca != null && !busca.isEmpty())) {
            String m = (marca == null || marca.equals("TODAS")) ? "" : marca;
            String b = (busca == null) ? "" : busca;
            return ResponseEntity.ok(repository.findByFiltros(categoriaAba.toUpperCase(), m, b, pageable));
        }

        return ResponseEntity.ok(repository.findByCategoriaAba(categoriaAba.toUpperCase(), pageable));
    }

    @GetMapping("/marcas/{categoriaAba}")
    public ResponseEntity<List<String>> listarMarcasPorAba(@PathVariable String categoriaAba) {
        return ResponseEntity.ok(repository.findDistinctMarcasByCategoriaAba(categoriaAba.toUpperCase()));
    }

    @PutMapping("/atualizar-custos/{id}")
    public ResponseEntity<?> atualizarCustos(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        Optional<ProdutoMesaCompra> optionalProduto = repository.findById(id);
        if (optionalProduto.isPresent()) {
            ProdutoMesaCompra p = optionalProduto.get();

            if (payload.containsKey("custosAtuais")) {
                Map<String, Object> custos = (Map<String, Object>) payload.get("custosAtuais");
                custos.forEach((k, v) -> p.getCustosFornecedores().put(k, Double.valueOf(v.toString())));
            }

            if (payload.containsKey("valorUnitarioDecidido")) {
                p.setValorUnitarioDecidido(Double.valueOf(payload.get("valorUnitarioDecidido").toString()));
            }

            repository.save(p);
            return ResponseEntity.ok(Map.of("mensagem", "Custos atualizados com sucesso!"));
        } else {
            return ResponseEntity.badRequest().body("Produto não encontrado.");
        }
    }

    // =========================================================================
    // 🔥 ROTAS PARA INJEÇÃO MANUAL, SINCRONIZAÇÃO DO BALANÇO E LIXEIRA DA MESA
    // =========================================================================

    @PostMapping
    public ResponseEntity<?> criarProdutoMesa(@RequestBody ProdutoMesaCompra produto) {
        try {
            return ResponseEntity.ok(repository.save(produto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarProdutoMesa(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Produto removido da mesa com sucesso!"));
    }

    @GetMapping("/cruzado/{categoriaAba}")
    public ResponseEntity<List<Map<String, Object>>> listarMesaCruzada(@PathVariable String categoriaAba) {
        return ResponseEntity.ok(service.obterDadosCruzadosMesa(categoriaAba));
    }


}