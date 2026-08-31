package br.com.carloseduardo.logisticasistema.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "historico_vendas")
public class HistoricoVenda {

    @Id
    private String id;

    // Nosso elo de ligação com tudo (ex: MD24, IP48)
    private String sku;

    // O nome do produto, só para termos como referência caso precise ler o banco direto
    private String produto;

    // Quantidade vendida que vem da planilha MENSAL
    private Integer vendaMensal = 0;

    // Quantidade vendida que vem da planilha SEMANAL
    private Integer vendaSemanal = 0;

    // A média da venda semanal.
    // OBS: Como você pediu a "Média só do semanal", vou calcular como vendaSemanal dividido por 7 dias para dar a média diária de saída.
    // Se a sua lógica for diferente, a gente muda a fórmula depois, sem estresse!
    private Double mediaDiaria;
}