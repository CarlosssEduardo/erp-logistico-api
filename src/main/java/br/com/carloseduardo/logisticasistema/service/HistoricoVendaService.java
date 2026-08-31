package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.HistoricoVenda;
import br.com.carloseduardo.logisticasistema.repository.HistoricoVendaRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistoricoVendaService {

    @Autowired
    private HistoricoVendaRepository repository;

    // O "tipoUpload" vai dizer se estamos lendo a planilha "MENSAL" ou "SEMANAL"
    public void processarPlanilhaVendas(MultipartFile file, String tipoUpload) throws Exception {
        List<HistoricoVenda> vendasParaSalvar = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            // Vamos ler a aba "VENDAS" que você tem nos dois arquivos
            Sheet sheet = workbook.getSheet("VENDAS");
            if (sheet == null) {
                // Se por acaso alguém renomeou a aba, lê a primeira aba padrão
                sheet = workbook.getSheetAt(0);
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Pula o cabeçalho

                String sku = getValorTexto(row.getCell(0)); // Coluna A: SKU
                if (sku.isEmpty()) continue; // Se não tem SKU, ignora a linha

                String nomeProduto = getValorTexto(row.getCell(1)); // Coluna B: Produto
                Integer qtdVendido = getValorInteiro(row.getCell(2)); // Coluna C: Qtde. Vendido

                // Busca se esse SKU já tem um histórico no nosso banco
                HistoricoVenda historico = repository.findBySku(sku);
                if (historico == null) {
                    historico = new HistoricoVenda();
                    historico.setSku(sku);
                }

                historico.setProduto(nomeProduto);

                // Dependendo do botão que o usuário clicou no front-end, ele preenche o mês ou a semana
                if (tipoUpload.equalsIgnoreCase("MENSAL")) {
                    historico.setVendaMensal(qtdVendido);
                } else if (tipoUpload.equalsIgnoreCase("SEMANAL")) {
                    historico.setVendaSemanal(qtdVendido);
                    // Calcula a média baseada no semanal (ex: dividido por 7 dias úteis).
                    // Usamos Math.round para deixar bonitinho com 2 casas decimais.
                    double media = qtdVendido / 7.0;
                    historico.setMediaDiaria(Math.round(media * 100.0) / 100.0);
                }

                vendasParaSalvar.add(historico);
            }

            // Salva e atualiza tudo rapidamente no MongoDB
            repository.saveAll(vendasParaSalvar);
        }
    }

    private String getValorTexto(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }

    private Integer getValorInteiro(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Integer.parseInt(cell.getStringCellValue().trim()); } catch (Exception e) { return 0; }
        }
        return 0;
    }
}