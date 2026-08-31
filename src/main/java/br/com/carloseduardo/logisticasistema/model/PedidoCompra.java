package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "pedidos_compras")
public class PedidoCompra {

    @Id
    private String id;
    private Integer ordem;
    private String numeroPdc;
    private String nomeFornecedor;
    private String obs;

    // --- NOVOS CAMPOS DA SUA TELA DE PDC ---
    private String mes; // Ex: "JUNHO", "JULHO"

    // 🔥 NOVA COLUNA PARA A LOJA
    private String loja;

    private String statusPagamento; // Ex: "PAGO", "PENDENTE"
    private String statusLogistica; // Ex: "AGENDA", "A CAMINHO", "ENTREGUE"
    private String statusPedido;    // Ex: "EM ANDAMENTO", "FINALIZADO"

    private LocalDateTime dataPedido;

    private List<ItemPedido> itens = new ArrayList<>();

    private Double valorTotalPedido = 0.0;
    private Double valorFrete = 0.0;
    private Double creditoUtilizado = 0.0;
    private Double valorPagarFinal = 0.0;

}