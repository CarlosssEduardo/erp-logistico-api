package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.VendaMensalResumo;
import br.com.carloseduardo.logisticasistema.repository.VendaMensalResumoRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendaMensalService {

    @Autowired
    private VendaMensalResumoRepository repository;

    public Map<String, Object> processarPlanilhaVenda(MultipartFile file, Double metaInformada) throws Exception {
        Double faturamento = 0.0;

        // Mapa para Agrupar Produtos (A Chave é o nome do produto)
        Map<String, Map<String, Object>> agregacao = new HashMap<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Pula cabeçalho

                String produto = getValorTexto(row.getCell(2)); // Coluna C (Produto)
                if (produto.isEmpty()) continue;

                Double precoUnitario = getValorNumerico(row.getCell(4)); // Coluna E (Preço)

                // 1. Soma da Coluna E (Faturamento Bruto)
                faturamento += precoUnitario;

                // 2. Agregação Inteligente (Conta 1 para cada vez que a linha se repete)
                if (!agregacao.containsKey(produto)) {
                    Map<String, Object> dadosProduto = new HashMap<>();
                    dadosProduto.put("produto", produto);
                    dadosProduto.put("qtde", 1); // Começa com 1 repetição
                    dadosProduto.put("total", precoUnitario);
                    agregacao.put(produto, dadosProduto);
                } else {
                    Map<String, Object> dadosProduto = agregacao.get(produto);
                    dadosProduto.put("qtde", (Integer) dadosProduto.get("qtde") + 1); // +1 repetição
                    dadosProduto.put("total", (Double) dadosProduto.get("total") + precoUnitario); // Soma o valor
                }
            }
        }

        // Prepara a lista final calculando o Valor Unitário Médio
        List<Map<String, Object>> detalhamento = new ArrayList<>();
        for (Map<String, Object> item : agregacao.values()) {
            Double total = (Double) item.get("total");
            Integer qtde = (Integer) item.get("qtde");

            // Tira a média caso o mesmo produto tenha sido vendido por preços diferentes
            item.put("valorUnitario", total / qtde);
            detalhamento.add(item);
        }

        // Ordena o Detalhamento por Faturamento (Do que deu mais dinheiro pro menor)
        detalhamento.sort((m1, m2) -> Double.compare((Double) m2.get("total"), (Double) m1.get("total")));

        // Ordena o Ranking (Top 20) pela QUANTIDADE de vezes que repetiu
        List<Map<String, Object>> top20 = detalhamento.stream()
                .sorted((e1, e2) -> Integer.compare((Integer) e2.get("qtde"), (Integer) e1.get("qtde")))
                .limit(20)
                .collect(Collectors.toList());

        // Calcula a Meta Diária com Gordura (+15%)
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.of(hoje.getYear(), hoje.getMonth());
        int diasRestantes = 0;

        // Conta dias úteis de "Amanhã" até o fim do mês
        for (int d = hoje.getDayOfMonth() + 1; d <= mesAtual.lengthOfMonth(); d++) {
            LocalDate dataFutura = LocalDate.of(hoje.getYear(), hoje.getMonth(), d);
            if (dataFutura.getDayOfWeek().getValue() != 7) { // 7 = Domingo
                diasRestantes++;
            }
        }

        Double metaDiaria = 0.0;
        Double faltaParaMeta = metaInformada - faturamento;

        if (diasRestantes > 0 && faltaParaMeta > 0) {
            // Divide o que falta pelos dias e joga 15% de margem de segurança!
            metaDiaria = (faltaParaMeta / diasRestantes) * 1.15;
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("faturamento", faturamento);
        resposta.put("top20", top20);
        resposta.put("detalhamento", detalhamento); // Detalhamento agora é limpo e agrupado
        resposta.put("meta", metaInformada);
        resposta.put("faltaParaMeta", Math.max(0, faltaParaMeta));
        resposta.put("diasRestantes", diasRestantes);
        resposta.put("metaDiariaComGordura", metaDiaria);

        return resposta;
    }

    public VendaMensalResumo salvarResultadoMes(VendaMensalResumo resumo) {
        Optional<VendaMensalResumo> existente = repository.findByMesReferencia(resumo.getMesReferencia());
        if (existente.isPresent()) {
            resumo.setId(existente.get().getId());
        }
        return repository.save(resumo);
    }

    public List<VendaMensalResumo> listarMesesSalvos() {
        return repository.findAll();
    }

    // --- Auxiliares ---
    private String getValorTexto(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        return "";
    }

    private Double getValorNumerico(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue().replace("R$", "").replace(".", "").replace(",", ".").trim()); }
            catch (Exception e) { return 0.0; }
        }
        return 0.0;
    }
}