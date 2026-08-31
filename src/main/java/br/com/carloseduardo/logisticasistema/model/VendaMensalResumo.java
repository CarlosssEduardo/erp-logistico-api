package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "vendas_mensais_resumo")
public class VendaMensalResumo {
    @Id
    private String id;
    private String mesReferencia; // Ex: "AGOSTO/2026"
    private Double faturamentoBruto;
    private Double meta;

    // Guardaremos o Ranking em um Mapa ou Lista
    private List<Map<String, Object>> top20Campeoes;

    // Todos os itens vendidos no mês
    private List<Map<String, Object>> detalhamentoVendas;
}