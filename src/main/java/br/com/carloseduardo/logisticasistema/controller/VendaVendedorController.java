package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.VendaVendedorResumo;
import br.com.carloseduardo.logisticasistema.service.VendaVendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vendedores")
public class VendaVendedorController {

    @Autowired
    private VendaVendedorService service;

    @PostMapping("/processar")
    public ResponseEntity<?> processar(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(service.processarPlanilhaVendedores(file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar: " + e.getMessage());
        }
    }

    @PostMapping("/salvar")
    public ResponseEntity<?> salvar(@RequestBody VendaVendedorResumo resumo) {
        return ResponseEntity.ok(service.salvarResultado(resumo));
    }

    @GetMapping("/historico")
    public ResponseEntity<?> listarHistorico() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarVendedor(@PathVariable String id) {
        service.deletarPorId(id);
        return ResponseEntity.ok().build();
    }
}