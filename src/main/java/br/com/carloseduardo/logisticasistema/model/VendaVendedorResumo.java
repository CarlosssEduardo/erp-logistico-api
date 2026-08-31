package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "vendas_vendedores_resumo")
public class VendaVendedorResumo {
    @Id
    private String id;
    private String mesReferencia;
    private String vendedor;
    private Double faturamento;
    private Double meta;
    private Double quantidadeItens;
    private List<Map<String, Object>> top10Clientes;
    private List<Map<String, Object>> top20Itens;
}