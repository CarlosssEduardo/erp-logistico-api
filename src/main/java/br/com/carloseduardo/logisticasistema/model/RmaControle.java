package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "rma_controle")
public class RmaControle {
    @Id
    private String id;
    private Integer ordem;

    // Nomes padronizados
    private String nomeFornecedor;
    private String statusLogistica = "AGENDAR";
    private String numeroPdc;
    private String mes;

    // Datas
    private LocalDateTime dataPedido;
    private LocalDateTime dataChegada; // 🔥 NOVO

    // Valores
    private Double valorTotalPedido = 0.0;
    private Double valorFrete = 0.0;

    // Observações e Itens
    private String obs; // 🔥 NOVO
    private List<ItemPedido> itens = new ArrayList<>(); // 🔥 NOVO (Lista das peças devolvidas)
}