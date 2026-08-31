package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.HistoricoVenda;
import br.com.carloseduardo.logisticasistema.repository.HistoricoVendaRepository;
import br.com.carloseduardo.logisticasistema.service.HistoricoVendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/vendas")
public class HistoricoVendaController {

    @Autowired
    private HistoricoVendaService service;

    @Autowired
    private HistoricoVendaRepository repository;

    // Rota de Upload. Exemplo: POST /api/vendas/upload/MENSAL
    @PostMapping("/upload/{tipo}")
    public ResponseEntity<?> fazerUploadVendas(@PathVariable String tipo, @RequestParam("file") MultipartFile file) {
        try {
            service.processarPlanilhaVendas(file, tipo);
            return ResponseEntity.ok(Map.of("mensagem", "Planilha de Venda " + tipo + " processada com sucesso!"));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar planilha de vendas: " + e.getMessage());
        }
    }

    // Rota que a Mesa de Compras vai usar para puxar o Giro de um SKU
    @GetMapping("/{sku}")
    public ResponseEntity<?> buscarGiroPorSku(@PathVariable String sku) {
        HistoricoVenda historico = repository.findBySku(sku);
        if (historico != null) {
            return ResponseEntity.ok(historico);
        } else {
            // Retorna um histórico zerado caso a gente nunca tenha vendido esse item ainda
            HistoricoVenda zerado = new HistoricoVenda();
            zerado.setSku(sku);
            return ResponseEntity.ok(zerado);
        }
    }

    // Rota para a Análise de Estoque puxar todas as vendas cadastradas
    @GetMapping("/todas")
    public ResponseEntity<?> listarTodasVendas() {
        return ResponseEntity.ok(repository.findAll());
    }
}