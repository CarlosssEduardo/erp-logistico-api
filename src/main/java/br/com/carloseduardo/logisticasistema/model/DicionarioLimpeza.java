package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "dicionario_limpeza")
public class DicionarioLimpeza {

    @Id
    private String id;

    // A palavra que você quer que o sistema apague dos nomes (ex: "DIAMONDS", "ZL", "C/A")
    private String palavraRemover;
}