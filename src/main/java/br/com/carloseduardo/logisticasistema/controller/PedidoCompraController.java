package br.com.carloseduardo.logisticasistema.controller;

import br.com.carloseduardo.logisticasistema.model.FornecedorCredito;
import br.com.carloseduardo.logisticasistema.model.PedidoCompra;
import br.com.carloseduardo.logisticasistema.repository.FornecedorCreditoRepository;
import br.com.carloseduardo.logisticasistema.repository.PedidoCompraRepository;
import br.com.carloseduardo.logisticasistema.service.PedidoCompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoCompraController {

    @Autowired
    private PedidoCompraService service;

    @Autowired
    private PedidoCompraRepository repository;

    @Autowired
    private FornecedorCreditoRepository fornecedorCreditoRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> fazerUploadPdc(@RequestParam("file") MultipartFile file) {
        try {
            service.processarPlanilhaPdc(file);
            return ResponseEntity.ok(Map.of("mensagem", "PDCs e RMAs atualizados com sucesso!"));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar planilha de PDCs: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> criarPedido(@RequestBody PedidoCompra pedido) {
        try {
            boolean isNovoPedido = (pedido.getId() == null || pedido.getId().isEmpty());
            double diferencaCredito = 0.0;

            if (isNovoPedido) {
                Optional<PedidoCompra> ultimoOpt = repository.findTopByOrderByOrdemDesc();
                if (ultimoOpt.isPresent() && ultimoOpt.get().getOrdem() != null) {
                    pedido.setOrdem(ultimoOpt.get().getOrdem() + 1);
                } else {
                    pedido.setOrdem(1);
                }
                pedido.setDataPedido(java.time.LocalDateTime.now());

                if (pedido.getCreditoUtilizado() != null) {
                    diferencaCredito = pedido.getCreditoUtilizado();
                }

            } else {
                PedidoCompra existente = repository.findById(pedido.getId()).orElse(null);
                if (existente != null) {
                    pedido.setOrdem(existente.getOrdem());
                    pedido.setDataPedido(existente.getDataPedido());

                    // 🔥 MÁGICA: Calcula se você aumentou ou diminuiu o crédito usado no extrato
                    double creditoAntigo = existente.getCreditoUtilizado() != null ? existente.getCreditoUtilizado() : 0.0;
                    double creditoNovo = pedido.getCreditoUtilizado() != null ? pedido.getCreditoUtilizado() : 0.0;
                    diferencaCredito = creditoNovo - creditoAntigo;
                }
            }

            // 🔥 ABATE (OU DEVOLVE) O DINHEIRO DA CARTEIRA
            if (diferencaCredito != 0.0 && pedido.getNomeFornecedor() != null) {
                Optional<FornecedorCredito> optCredito = fornecedorCreditoRepository.findAll().stream()
                        .filter(c -> c.getNomeFornecedor() != null && c.getNomeFornecedor().equalsIgnoreCase(pedido.getNomeFornecedor()))
                        .findFirst();

                if (optCredito.isPresent()) {
                    FornecedorCredito carteira = optCredito.get();
                    // Se a diferença for positiva (usou mais crédito), diminui do saldo. Se for negativa, devolve pro saldo.
                    double saldoAtualizado = carteira.getValorCredito() - diferencaCredito;
                    carteira.setValorCredito(saldoAtualizado);
                    fornecedorCreditoRepository.save(carteira);
                }
            }

            if (pedido.getNumeroPdc() == null || pedido.getNumeroPdc().trim().isEmpty()) {
                pedido.setNumeroPdc("");
            }

            PedidoCompra novoPedido = repository.save(pedido);
            return ResponseEntity.ok(novoPedido);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<PedidoCompra>> listarPedidos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(repository.findAllByOrderByOrdemDesc(pageable));
    }

    @GetMapping("/{id}/whatsapp")
    public ResponseEntity<?> obterTextoWhatsapp(@PathVariable String id) {
        try {
            String texto = service.gerarTextoWhatsApp(id);
            return ResponseEntity.ok(Map.of("texto", texto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/{id}/nao-vem")
    public ResponseEntity<?> processarNaoVem(@PathVariable String id, @RequestBody Map<String, String> payload) {
        try {
            String textoFornecedor = payload.get("textoFornecedor");
            PedidoCompra pedidoAtualizado = service.processarTextoNaoVem(id, textoFornecedor);
            return ResponseEntity.ok(pedidoAtualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirPedido(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Pedido excluído com sucesso!"));
    }

    @PostMapping("/{id}/atualizar")
    public ResponseEntity<?> atualizarCampo(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        PedidoCompra pedido = repository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (payload.containsKey("valorFrete")) pedido.setValorFrete(Double.valueOf(payload.get("valorFrete").toString()));
        if (payload.containsKey("valorTotalPedido")) pedido.setValorTotalPedido(Double.valueOf(payload.get("valorTotalPedido").toString()));
        if (payload.containsKey("numeroPdc")) pedido.setNumeroPdc(payload.get("numeroPdc").toString().toUpperCase());
        if (payload.containsKey("statusPagamento")) pedido.setStatusPagamento(payload.get("statusPagamento").toString());
        if (payload.containsKey("statusLogistica")) pedido.setStatusLogistica(payload.get("statusLogistica").toString());
        if (payload.containsKey("statusPedido")) pedido.setStatusPedido(payload.get("statusPedido").toString());
        if (payload.containsKey("loja")) pedido.setLoja(payload.get("loja").toString().toUpperCase());

        return ResponseEntity.ok(repository.save(pedido));
    }
}