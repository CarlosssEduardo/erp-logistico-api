package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.DicionarioLimpeza;
import br.com.carloseduardo.logisticasistema.model.ProdutoMesaCompra;
import br.com.carloseduardo.logisticasistema.repository.DicionarioLimpezaRepository;
import br.com.carloseduardo.logisticasistema.repository.ProdutoMesaCompraRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProdutoMesaCompraService {

    @Autowired
    private ProdutoMesaCompraRepository mesaCompraRepository;

    @Autowired
    private DicionarioLimpezaRepository dicionarioRepository;

    public void processarPlanilhaBase(MultipartFile file, String categoriaAba) throws Exception {
        List<ProdutoMesaCompra> produtosParaSalvar = new ArrayList<>();
        List<DicionarioLimpeza> palavrasDicionario = dicionarioRepository.findAll();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) throw new RuntimeException("Planilha sem cabeçalho!");

            // 🔥 RASTREADORES DINÂMICOS DE COLUNAS
            int indexMarca = -1;
            int indexModelo = -1;
            int indexProduto = -1;

            Map<String, Integer> colunasCodigos = new HashMap<>();
            Map<String, Integer> colunasCustos = new HashMap<>();

            // 1. LÊ O CABEÇALHO PARA DESCOBRIR ONDE ESTÁ CADA COISA
            for (Cell cell : headerRow) {
                String header = getValorTexto(cell).toUpperCase().trim();
                int colIdx = cell.getColumnIndex();

                if (header.equals("MARCA")) {
                    indexMarca = colIdx;
                } else if (header.equals("MODELO")) {
                    indexModelo = colIdx;
                } else if (header.equals("PRODUTO") || header.equals("DESCRIÇÃO") || header.equals("DESCRICAO")) {
                    indexProduto = colIdx;
                } else if (header.startsWith("CODIGO") || header.startsWith("CÓDIGO")) {
                    // Extrai o nome do Fornecedor (Ex: "CODIGO ZL" -> "ZL")
                    String fornecedor = header.replace("CÓDIGO", "").replace("CODIGO", "").replace(":", "").replace("-", "").trim();
                    if (!fornecedor.isEmpty()) colunasCodigos.put(fornecedor, colIdx);
                } else if (header.contains("CUSTO")) {
                    // Extrai o nome do Fornecedor (Ex: "ZL: CUSTO ATUAL" -> "ZL")
                    String fornecedor = "";
                    if (header.contains(":")) {
                        fornecedor = header.split(":")[0].trim();
                    } else {
                        fornecedor = header.replace("CUSTO ATUAL", "").replace("CUSTO", "").trim();
                    }
                    if (!fornecedor.isEmpty()) colunasCustos.put(fornecedor, colIdx);
                }
            }

            // 2. PROCESSA AS LINHAS BASEADO NO MAPEAMENTO DINÂMICO
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Pula o cabeçalho

                String descricaoOriginal = indexProduto != -1 ? getValorTexto(row.getCell(indexProduto)) : "";
                if (descricaoOriginal.isEmpty()) continue; // Produto é obrigatório

                String marca = indexMarca != -1 ? getValorTexto(row.getCell(indexMarca)) : "";
                String modelo = indexModelo != -1 ? getValorTexto(row.getCell(indexModelo)) : "";

                String descricaoLimpa = limparDescricaoProduto(descricaoOriginal, palavrasDicionario);

                List<ProdutoMesaCompra> existentes = mesaCompraRepository.findByDescricaoLimpaAndCategoriaAba(descricaoLimpa, categoriaAba);

                ProdutoMesaCompra produto;
                if (existentes.isEmpty()) {
                    produto = new ProdutoMesaCompra();
                    produto.setCategoriaAba(categoriaAba.toUpperCase());
                } else {
                    produto = existentes.get(0);
                }

                produto.setDescricaoLimpa(descricaoLimpa);
                if (!marca.isEmpty()) produto.setMarca(marca.toUpperCase());
                if (!modelo.isEmpty()) produto.setModelo(modelo.toUpperCase());

                // LÊ TODOS OS CÓDIGOS DINAMICAMENTE
                for (Map.Entry<String, Integer> entry : colunasCodigos.entrySet()) {
                    String cod = getValorTexto(row.getCell(entry.getValue()));
                    if (!cod.isEmpty()) {
                        produto.getCodigosFornecedores().put(entry.getKey(), cod);
                    }
                }

                // LÊ TODOS OS CUSTOS DINAMICAMENTE
                for (Map.Entry<String, Integer> entry : colunasCustos.entrySet()) {
                    Double custo = getValorNumerico(row.getCell(entry.getValue()));
                    if (custo > 0) {
                        produto.getCustosFornecedores().put(entry.getKey(), custo);
                    }
                }

                produtosParaSalvar.add(produto);
            }

            mesaCompraRepository.saveAll(produtosParaSalvar);
        }
    }

    private String limparDescricaoProduto(String descricaoOriginal, List<DicionarioLimpeza> palavrasDicionario) {
        if (descricaoOriginal == null) return "";
        String textoLimpo = descricaoOriginal;
        for (DicionarioLimpeza dic : palavrasDicionario) {
            String palavra = dic.getPalavraRemover();
            textoLimpo = textoLimpo.replaceAll("(?i)\\b" + palavra + "\\b", "");
        }
        return textoLimpo.replaceAll("\\s+", " ").trim();
    }

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
}