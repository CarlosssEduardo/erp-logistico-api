package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data // O Lombok cria os Getters e Setters automaticamente
@Document(collection = "fornecedor_itens_novos")
public class FornecedorItemNovo {

    @Id
    private String id;
    private String fornecedor;      // Ex: ASSUGAR
    private String codigoFornecedor; // Ex: ASS28
    private String descricaoOriginal; // Ex: MOTOROLA G13/G23/G34/G53 S/ARO ASSUGAR LATA
    private double valor;
    private int quantidade;

    // Vínculo com o Produto Universal (Mesa de Compras / Estoque)
    private String skuUniversal;     // SKU do produto oficial vinculado
    private String produtoUniversalNome; // Descrição limpa do produto universal
    private boolean vinculado;       // true se já foi associado
}