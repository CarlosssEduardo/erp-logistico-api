package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "fornecedores_creditos")
public class FornecedorCredito {
    @Id private String id;
    private String nomeFornecedor;
    private Double valorCredito = 0.0;
}