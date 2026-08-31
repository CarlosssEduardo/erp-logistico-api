package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.RmaControle;
import br.com.carloseduardo.logisticasistema.repository.RmaControleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rma")
public class RmaControleController {

    @Autowired
    private RmaControleRepository repository;

    @GetMapping
    public ResponseEntity<Page<RmaControle>> listarRma(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<?> criarRma(@RequestBody RmaControle rma) {
        try {
            if (rma.getId() == null || rma.getId().isEmpty()) {
                Optional<RmaControle> ultimoOpt = repository.findTopByOrderByOrdemDesc();

                if (ultimoOpt.isPresent() && ultimoOpt.get().getOrdem() != null) {
                    rma.setOrdem(ultimoOpt.get().getOrdem() + 1);
                } else {
                    rma.setOrdem(1);
                }
                rma.setDataPedido(LocalDateTime.now());
            }

            if (rma.getNumeroPdc() == null || rma.getNumeroPdc().trim().isEmpty()) {
                rma.setNumeroPdc("");
            }

            RmaControle novoRma = repository.save(rma);
            return ResponseEntity.ok(novoRma);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // 🔥 NOVA ROTA PARA O BOTÃO DE LIXEIRA FUNCIONAR!
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirRma(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "RMA excluído com sucesso!"));
    }

    @PostMapping("/{id}/atualizar")
    public ResponseEntity<?> atualizarCampo(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        RmaControle rma = repository.findById(id).orElseThrow(() -> new RuntimeException("RMA não encontrado"));

        if (payload.containsKey("valorFrete")) rma.setValorFrete(Double.valueOf(payload.get("valorFrete").toString()));
        if (payload.containsKey("valorTotalPedido")) rma.setValorTotalPedido(Double.valueOf(payload.get("valorTotalPedido").toString()));
        if (payload.containsKey("numeroPdc")) rma.setNumeroPdc(payload.get("numeroPdc").toString().toUpperCase());
        if (payload.containsKey("statusLogistica")) rma.setStatusLogistica(payload.get("statusLogistica").toString().toUpperCase());
        if (payload.containsKey("obs")) rma.setObs(payload.get("obs").toString());

        // 🔥 AGORA O BACK-END ACEITA A MUDANÇA DO MÊS PELO MENU SUSPENSO
        if (payload.containsKey("mes")) rma.setMes(payload.get("mes").toString().toUpperCase());

        try {
            if (payload.containsKey("dataPedido")) {
                String dataStr = payload.get("dataPedido").toString();
                if (!dataStr.isEmpty()) {
                    if (!dataStr.contains("T")) dataStr += "T12:00:00";
                    rma.setDataPedido(LocalDateTime.parse(dataStr));
                } else {
                    rma.setDataPedido(null);
                }
            }
            if (payload.containsKey("dataChegada")) {
                String dataStr = payload.get("dataChegada").toString();
                if (!dataStr.isEmpty()) {
                    if (!dataStr.contains("T")) dataStr += "T12:00:00";
                    rma.setDataChegada(LocalDateTime.parse(dataStr));
                } else {
                    rma.setDataChegada(null);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar data: " + e.getMessage());
        }

        return ResponseEntity.ok(repository.save(rma));
    }
}