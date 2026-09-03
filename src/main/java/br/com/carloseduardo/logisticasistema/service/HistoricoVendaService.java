package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.HistoricoVenda;
import br.com.carloseduardo.logisticasistema.repository.HistoricoVendaRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HistoricoVendaService {

    @Autowired
    private HistoricoVendaRepository repository;

    public void processarPlanilhaVendas(MultipartFile file, String tipoUpload) throws Exception {
        List<HistoricoVenda> todosExistentes = repository.findAll();
        Map<String, HistoricoVenda> mapaExistentes = new HashMap<>();
        for (HistoricoVenda h : todosExistentes) {
            mapaExistentes.put(h.getSku(), h);
        }

        Set<HistoricoVenda> vendasParaSalvar = new HashSet<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet("VENDAS");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Pula o cabeçalho

                String sku = getValorTexto(row.getCell(0)); // Coluna A: SKU
                if (sku.isEmpty()) continue;

                String nomeProduto = getValorTexto(row.getCell(1)); // Coluna B: Produto
                Integer qtdVendido = getValorInteiro(row.getCell(2)); // Coluna C: Qtde. Vendido

                HistoricoVenda historico = mapaExistentes.get(sku);
                if (historico == null) {
                    historico = new HistoricoVenda();
                    historico.setSku(sku);
                    mapaExistentes.put(sku, historico);
                }

                historico.setProduto(nomeProduto);

                if (tipoUpload.equalsIgnoreCase("MENSAL")) {
                    historico.setVendaMensal(qtdVendido);
                } else if (tipoUpload.equalsIgnoreCase("SEMANAL")) {
                    historico.setVendaSemanal(qtdVendido);
                    double media = qtdVendido / 7.0;
                    historico.setMediaDiaria(Math.round(media * 100.0) / 100.0);
                }

                vendasParaSalvar.add(historico);
            }

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