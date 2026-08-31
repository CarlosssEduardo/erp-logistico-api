package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.VendaMensalResumo;
import br.com.carloseduardo.logisticasistema.service.VendaMensalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vendas-mensais")
public class VendaMensalController {

    @Autowired
    private VendaMensalService service;

    @PostMapping("/processar")
    public ResponseEntity<?> processarPlanilha(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "meta", defaultValue = "100000") Double meta) {
        try {
            return ResponseEntity.ok(service.processarPlanilhaVenda(file, meta));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar: " + e.getMessage());
        }
    }

    @PostMapping("/salvar")
    public ResponseEntity<?> salvarMes(@RequestBody VendaMensalResumo resumo) {
        return ResponseEntity.ok(service.salvarResultadoMes(resumo));
    }

    @GetMapping("/historico")
    public ResponseEntity<?> listarHistorico() {
        return ResponseEntity.ok(service.listarMesesSalvos());
    }
}