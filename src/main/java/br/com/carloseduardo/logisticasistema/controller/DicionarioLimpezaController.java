package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.DicionarioLimpeza;
import br.com.carloseduardo.logisticasistema.repository.DicionarioLimpezaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dicionario")
public class DicionarioLimpezaController {

    @Autowired
    private DicionarioLimpezaRepository repository;

    @GetMapping
    public List<DicionarioLimpeza> listarTodos() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> adicionarPalavra(@RequestBody DicionarioLimpeza dicionario) {
        if (repository.existsByPalavraRemoverIgnoreCase(dicionario.getPalavraRemover())) {
            return ResponseEntity.badRequest().body("Esta palavra já está no dicionário!");
        }
        return ResponseEntity.ok(repository.save(dicionario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerPalavra(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok("Palavra removida do dicionário.");
    }

}