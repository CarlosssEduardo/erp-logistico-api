package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data // O Lombok cria os Getters e Setters automaticamente
@Document(collection = "estoque_bruto") // Nome da tabela no MongoDB
public class ProdutoEstoque {

    @Id
    private String id;

    // Colunas da Planilha Balanço
    private String categoria;       // Coluna A
    private String sku;             // Coluna B (Nossa chave principal)
    private String marca;           // Coluna C
    private String item;            // Coluna D (Descrição do Produto)
    private String usadoNosModelos; // Coluna E
    private String localizacao;     // Coluna F
    private Integer quantidade;     // Coluna G
    private String unidade;         // Coluna H
    private Double valorUnitario;   // Coluna I
    private Double valorTotal;      // Coluna J
    private String codBarras;       // Coluna K

    // Nossas variáveis de inteligência de sistema
    private boolean classificado;   // true = tem categoria certa / false = vai pro "Inventário de Novos"
    private boolean naMesaDeCompras;// true = o usuário já clicou no botão e jogou pra aba de compras

    // 🔥 NOVA COLUNA DE INTELIGÊNCIA MESA DE TELAS
    private String skuMestre;       // Guarda o SKU universal (Ex: IP39) quando o produto for de um fornecedor (Ex: ASSUGAR)
}