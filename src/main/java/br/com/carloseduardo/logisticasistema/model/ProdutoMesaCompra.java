package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@Document(collection = "produtos_mesa_compra")
public class ProdutoMesaCompra {
    @Id
    private String id;
    private String categoriaAba;
    private String descricaoLimpa;
    private String marca;


    // 🔥 NOVO CAMPO PARA O MODELO
    private String modelo;

    // Guarda os códigos do fornecedor. Ex: {"DIAMONDS": "IP39", "ZL": "IPZ14"}
    private Map<String, String> codigosFornecedores = new HashMap<>();

    // Guarda os custos vindos do Excel (Colunas E e F). Ex: {"DIAMONDS": 72.00, "ZL": 70.00}
    private Map<String, Double> custosFornecedores = new HashMap<>();

    // Valor unitário final editável (nosso preço do pedido)
    private Double valorUnitarioDecidido = 0.0;
}