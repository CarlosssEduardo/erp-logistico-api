package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.VendaVendedorResumo;
import br.com.carloseduardo.logisticasistema.repository.VendaVendedorRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendaVendedorService {

    @Autowired
    private VendaVendedorRepository repository;

    // Classe auxiliar interna para agrupar os dados enquanto lê
    class VendedorStats {
        String nome = "";
        Double faturamento = 0.0;
        Double qtdItens = 0.0;
        Map<String, Integer> clientesCount = new HashMap<>();
        Map<String, Integer> itensCount = new HashMap<>();
    }

    public List<Map<String, Object>> processarPlanilhaVendedores(MultipartFile file) throws Exception {
        Map<String, VendedorStats> agrupamento = new HashMap<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Pula o cabeçalho

                String vendedor = getValorTexto(row.getCell(2)); // Coluna C
                if (vendedor.isEmpty()) continue;

                String cliente = getValorTexto(row.getCell(3));  // Coluna D
                String item = getValorTexto(row.getCell(8));     // Coluna I
                Double qtde = getValorNumerico(row.getCell(9));  // Coluna J
                Double valorUnitario = getValorNumerico(row.getCell(10)); // Coluna K

                VendedorStats stats = agrupamento.computeIfAbsent(vendedor, k -> {
                    VendedorStats s = new VendedorStats();
                    s.nome = vendedor;
                    return s;
                });

                // Faturamento = Quantidade * Valor Unitário
                stats.faturamento += (valorUnitario * qtde);
                stats.qtdItens += qtde;

                if (!cliente.isEmpty()) {
                    stats.clientesCount.put(cliente, stats.clientesCount.getOrDefault(cliente, 0) + 1);
                }
                if (!item.isEmpty()) {
                    stats.itensCount.put(item, stats.itensCount.getOrDefault(item, 0) + 1);
                }
            }
        }

        List<Map<String, Object>> resultados = new ArrayList<>();

        for (VendedorStats stats : agrupamento.values()) {
            Map<String, Object> res = new HashMap<>();
            res.put("vendedor", stats.nome);
            res.put("faturamento", stats.faturamento);
            res.put("quantidadeItens", stats.qtdItens);

            // Ordena os Clientes (Top 10 que mais aparecem)
            List<Map<String, Object>> top10Clientes = stats.clientesCount.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(10)
                    .map(e -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("nome", e.getKey());
                        map.put("compras", e.getValue());
                        return map;
                    })
                    .collect(Collectors.toList());

            // Ordena os Itens (Top 20 que mais aparecem)
            List<Map<String, Object>> top20Itens = stats.itensCount.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(20)
                    .map(e -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("nome", e.getKey());
                        map.put("repeticoes", e.getValue());
                        return map;
                    })
                    .collect(Collectors.toList());

            res.put("top10Clientes", top10Clientes);
            res.put("top20Itens", top20Itens);
            resultados.add(res);
        }

        // Ordena a lista de vendedores pelo que mais faturou
        resultados.sort((m1, m2) -> Double.compare((Double) m2.get("faturamento"), (Double) m1.get("faturamento")));
        return resultados;
    }

    public VendaVendedorResumo salvarResultado(VendaVendedorResumo resumo) {
        // Trava de Inteligência: Evita duplicação do mesmo vendedor no mesmo mês
        List<VendaVendedorResumo> existentes = repository.findByMesReferencia(resumo.getMesReferencia());
        for (VendaVendedorResumo ext : existentes) {
            if (ext.getVendedor().equalsIgnoreCase(resumo.getVendedor())) {
                resumo.setId(ext.getId()); // Se achar, pega o ID antigo para ATUALIZAR
                break;
            }
        }
        return repository.save(resumo);
    }



    public List<VendaVendedorResumo> listarPorMes(String mes) {
        return repository.findByMesReferencia(mes);
    }

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

    public void deletarPorId(String id) {
        repository.deleteById(id);
    }

    public List<VendaVendedorResumo> listarTodos() {
        return repository.findAll();
    }
}