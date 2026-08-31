package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;

@Data
public class ItemPedido {
    // Referência direta ao Banco de Dados (Estoque Bruto)
    private String produtoId;

    // O nosso código e o código do fornecedor para não ter confusão
    private String skuUniversal;
    private String skuFornecedor;

    // Aquele nome bonitinho já sem o "ZL", "DIAMONDS"
    private String descricaoLimpa;

    private Integer quantidadePedida;
    private Double valorUnitario;
    private Double subtotal; // Qtd * Valor

    // INTELIGÊNCIA: Se o fornecedor avisar no WhatsApp que a peça faltou, a gente marca isso como true
    private boolean naoVem = false;
}