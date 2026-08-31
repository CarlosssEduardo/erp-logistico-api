package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.FornecedorCredito;
import br.com.carloseduardo.logisticasistema.repository.FornecedorCreditoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/creditos")
public class FornecedorCreditoController {

    @Autowired private FornecedorCreditoRepository repository;

    @GetMapping
    public List<FornecedorCredito> listar() { return repository.findAll(); }

    // 🔥 A PORTA QUE FALTAVA: O React bate aqui enviando o nome (Ex: /api/creditos/DIAMONDS)
    @GetMapping("/{nomeFornecedor}")
    public ResponseEntity<?> buscarCreditoPorFornecedor(@PathVariable String nomeFornecedor) {
        Optional<FornecedorCredito> credito = repository.findAll().stream()
                .filter(c -> c.getNomeFornecedor() != null && c.getNomeFornecedor().equalsIgnoreCase(nomeFornecedor))
                .findFirst();

        // Se achar o crédito, devolve ele. Se não achar, devolve 0 pra não quebrar a tela!
        if (credito.isPresent()) {
            return ResponseEntity.ok(credito.get());
        } else {
            return ResponseEntity.ok(Map.of("valor", 0.0));
        }
    }

    @PostMapping
    public FornecedorCredito salvar(@RequestBody FornecedorCredito credito) {
        if(credito.getNomeFornecedor() != null) credito.setNomeFornecedor(credito.getNomeFornecedor().toUpperCase());
        return repository.save(credito);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}