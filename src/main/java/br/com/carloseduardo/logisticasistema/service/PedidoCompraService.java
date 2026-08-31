package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.PedidoCompra;
import br.com.carloseduardo.logisticasistema.model.RmaControle;
import br.com.carloseduardo.logisticasistema.repository.PedidoCompraRepository;
import br.com.carloseduardo.logisticasistema.repository.RmaControleRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoCompraService {

    @Autowired
    private PedidoCompraRepository repository;

    // 🔥 INJETANDO O REPOSITÓRIO DE RMA PARA SALVAR A SEGUNDA ABA DO EXCEL
    @Autowired
    private RmaControleRepository rmaRepository;

    public void processarPlanilhaPdc(MultipartFile file) throws Exception {
        List<PedidoCompra> pedidosParaSalvar = new ArrayList<>();
        List<RmaControle> rmasParaSalvar = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            // =========================================================================
            // 1. LENDO A ABA DE PEDIDOS DE COMPRA (Aba 1 - Índice 0)
            // =========================================================================
            Sheet sheetPedidos = workbook.getSheetAt(0);

            for (Row row : sheetPedidos) {
                if (row.getRowNum() == 0) continue; // Pula cabeçalho

                String fornecedor = getValorTexto(row.getCell(1));
                if (fornecedor.isEmpty()) continue;

                PedidoCompra pedido = new PedidoCompra();

                pedido.setOrdem(getValorInteiro(row.getCell(0)));
                pedido.setNomeFornecedor(fornecedor);
                pedido.setStatusPagamento(getValorTexto(row.getCell(2)));
                pedido.setStatusLogistica(getValorTexto(row.getCell(3)));
                pedido.setStatusPedido(getValorTexto(row.getCell(4)));
                pedido.setNumeroPdc(getValorTexto(row.getCell(5)));
                pedido.setMes(getValorTexto(row.getCell(6)));

                pedido.setValorTotalPedido(getValorNumerico(row.getCell(9)));
                pedido.setValorFrete(getValorNumerico(row.getCell(10)));
                pedido.setValorPagarFinal(getValorNumerico(row.getCell(11)));
                pedido.setCreditoUtilizado(getValorNumerico(row.getCell(12)));
                pedido.setObs(getValorTexto(row.getCell(13)));

                pedidosParaSalvar.add(pedido);
            }
            repository.saveAll(pedidosParaSalvar);

            // =========================================================================
            // 2. LENDO A ABA DE RMA (Aba 2 - Índice 1)
            // 🔥 Tenta ler a aba chamada "RMA" ou a segunda aba do Excel
            // =========================================================================
            Sheet sheetRma = workbook.getSheet("RMA");
            if (sheetRma == null && workbook.getNumberOfSheets() > 1) {
                sheetRma = workbook.getSheetAt(1); // Pega a aba 2 se o nome não for exato
            }

            if (sheetRma != null) {
                for (Row row : sheetRma) {
                    if (row.getRowNum() == 0) continue; // Pula cabeçalho

                    String fornecedorRma = getValorTexto(row.getCell(1)); // Assumindo que a coluna 1 é fornecedor
                    if (fornecedorRma.isEmpty()) continue;

                    RmaControle rma = new RmaControle();

                    // 🔥 Mapeamento para o RMA (Estou baseando na mesma ordem de colunas do PDC)
                    rma.setOrdem(getValorInteiro(row.getCell(0)));
                    rma.setNomeFornecedor(fornecedorRma);
                    rma.setStatusLogistica(getValorTexto(row.getCell(3))); // Coluna Logística
                    rma.setNumeroPdc(getValorTexto(row.getCell(5)));       // Coluna PDC
                    rma.setMes(getValorTexto(row.getCell(6)));             // Coluna Mês

                    // Colunas de Valores
                    rma.setValorTotalPedido(getValorNumerico(row.getCell(9)));
                    rma.setValorFrete(getValorNumerico(row.getCell(10)));

                    rmasParaSalvar.add(rma);
                }
                rmaRepository.saveAll(rmasParaSalvar);
            }
        }
    }

    public PedidoCompra criarPedido(PedidoCompra pedido) {
        return repository.save(pedido);
    }

    public String gerarTextoWhatsApp(String id) {
        Optional<PedidoCompra> pedidoOpt = repository.findById(id);
        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido não encontrado");
        }

        PedidoCompra p = pedidoOpt.get();
        StringBuilder sb = new StringBuilder();
        sb.append("*PEDIDO - ").append(p.getNomeFornecedor()).append("*\n\n");

        if (p.getItens() != null) {
            p.getItens().forEach(item -> {
                String desc = item.getDescricaoLimpa() != null ? item.getDescricaoLimpa() : "Item sem descrição";
                Integer qtd = item.getQuantidadePedida() != null ? item.getQuantidadePedida() : 0;
                sb.append("• ").append(desc).append(" - Qtd: ").append(qtd).append("\n");
            });
        }
        return sb.toString();
    }

    public PedidoCompra processarTextoNaoVem(String id, String textoFornecedor) {
        Optional<PedidoCompra> pedidoOpt = repository.findById(id);
        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido não encontrado");
        }

        PedidoCompra p = pedidoOpt.get();
        p.setObs((p.getObs() == null ? "" : p.getObs() + "\n") + "Não vem: " + textoFornecedor);
        return repository.save(p);
    }

    // --- MÉTODOS AUXILIARES ---
    private String getValorTexto(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }

    private Double getValorNumerico(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().replace("R$", "").replace(".", "").replace(",", ".").trim());
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private Integer getValorInteiro(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}