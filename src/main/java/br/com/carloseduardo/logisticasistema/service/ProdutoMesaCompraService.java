package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.DicionarioLimpeza;
import br.com.carloseduardo.logisticasistema.model.HistoricoVenda;
import br.com.carloseduardo.logisticasistema.model.ProdutoEstoque;
import br.com.carloseduardo.logisticasistema.model.ProdutoMesaCompra;
import br.com.carloseduardo.logisticasistema.repository.DicionarioLimpezaRepository;
import br.com.carloseduardo.logisticasistema.repository.ProdutoMesaCompraRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import br.com.carloseduardo.logisticasistema.repository.ProdutoEstoqueRepository;
import br.com.carloseduardo.logisticasistema.repository.HistoricoVendaRepository;

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
        List<DicionarioLimpeza> palavrasDicionario = dicionarioRepository.findAll();
        String categoriaUpper = categoriaAba.toUpperCase();

        // 🔥 OTIMIZAÇÃO: Busca TODOS os produtos existentes desta categoria de uma só vez
        List<ProdutoMesaCompra> listaExistentesDb = mesaCompraRepository.findByCategoriaAba(categoriaUpper);

        // Coloca em um Map para busca instantânea em memória (Chave: descricaoLimpa)
        Map<String, ProdutoMesaCompra> mapaExistentes = new HashMap<>();
        for (ProdutoMesaCompra p : listaExistentesDb) {
            mapaExistentes.put(p.getDescricaoLimpa(), p);
        }

        List<ProdutoMesaCompra> produtosParaSalvar = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) throw new RuntimeException("Planilha sem cabeçalho!");

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
                    String fornecedor = header.replace("CÓDIGO", "").replace("CODIGO", "").replace(":", "").replace("-", "").trim();
                    if (!fornecedor.isEmpty()) colunasCodigos.put(fornecedor, colIdx);
                } else if (header.contains("CUSTO")) {
                    String fornecedor = "";
                    if (header.contains(":")) {
                        fornecedor = header.split(":")[0].trim();
                    } else {
                        fornecedor = header.replace("CUSTO ATUAL", "").replace("CUSTO", "").trim();
                    }
                    if (!fornecedor.isEmpty()) colunasCustos.put(fornecedor, colIdx);
                }
            }

            // 2. PROCESSA AS LINHAS USANDO O MAPA EM MEMÓRIA (SEM CONSULTAR O BANCO DENTRO DO LOOP)
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String descricaoOriginal = indexProduto != -1 ? getValorTexto(row.getCell(indexProduto)) : "";
                if (descricaoOriginal.isEmpty()) continue;

                String marca = indexMarca != -1 ? getValorTexto(row.getCell(indexMarca)) : "";
                String modelo = indexModelo != -1 ? getValorTexto(row.getCell(indexModelo)) : "";

                String descricaoLimpa = limparDescricaoProduto(descricaoOriginal, palavrasDicionario);

                // Busca direto no Map da RAM (Zero latência de rede!)
                ProdutoMesaCompra produto = mapaExistentes.get(descricaoLimpa);

                if (produto == null) {
                    produto = new ProdutoMesaCompra();
                    produto.setCategoriaAba(categoriaUpper);
                    produto.setDescricaoLimpa(descricaoLimpa);
                    mapaExistentes.put(descricaoLimpa, produto); // Adiciona ao mapa caso repita na mesma planilha
                    produtosParaSalvar.add(produto);
                } else {
                    // Garante que vai salvar a atualização do produto existente
                    if (!produtosParaSalvar.contains(produto)) {
                        produtosParaSalvar.add(produto);
                    }
                }

                if (!marca.isEmpty()) produto.setMarca(marca.toUpperCase());
                if (!modelo.isEmpty()) produto.setModelo(modelo.toUpperCase());

                for (Map.Entry<String, Integer> entry : colunasCodigos.entrySet()) {
                    String cod = getValorTexto(row.getCell(entry.getValue()));
                    if (!cod.isEmpty()) {
                        produto.getCodigosFornecedores().put(entry.getKey(), cod);
                    }
                }

                for (Map.Entry<String, Integer> entry : colunasCustos.entrySet()) {
                    Double custo = getValorNumerico(row.getCell(entry.getValue()));
                    if (custo > 0) {
                        produto.getCustosFornecedores().put(entry.getKey(), custo);
                    }
                }
            }

            // Salva tudo de uma vez só no final (Batch Save)
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

    @Autowired
    private ProdutoEstoqueRepository estoqueRepository;
    @Autowired
    private HistoricoVendaRepository vendaRepository;

    public List<Map<String, Object>> obterDadosCruzadosMesa(String categoriaAba) {
        // 1. Busca a base da mesa
        List<ProdutoMesaCompra> mesa = mesaCompraRepository.findByCategoriaAba(categoriaAba.toUpperCase());

        // 2. Traz TODO o estoque e TODAS as vendas para a memória de uma vez (Zero latência de rede)
        List<ProdutoEstoque> todoEstoque = estoqueRepository.findAll();
        List<HistoricoVenda> todasVendas = vendaRepository.findAll();

        // 3. Converte para HashMaps para busca instantânea (O(1))
        // Nota: Ajuste o "getSku()" ou "getDescricaoLimpa()" de acordo com a chave que você usa para cruzar os dados
        Map<String, ProdutoEstoque> mapaEstoque = new HashMap<>();
        for (ProdutoEstoque e : todoEstoque) {
            mapaEstoque.put(e.getSku(), e);
        }

        Map<String, HistoricoVenda> mapaVendas = new HashMap<>();
        for (HistoricoVenda v : todasVendas) {
            mapaVendas.put(v.getSku(), v);
        }

        // 4. Monta o pacote final cruzado
        List<Map<String, Object>> respostaFinal = new ArrayList<>();

        for (ProdutoMesaCompra itemMesa : mesa) {
            Map<String, Object> linhaCruzada = new HashMap<>();
            linhaCruzada.put("mesa", itemMesa);

            // Exemplo: Cruzando usando o Código ZL ou a Descrição Limpa (Adapte para sua regra de negócio)
            String chaveCruzamento = itemMesa.getDescricaoLimpa(); // ou itemMesa.getCodigosFornecedores().get("ZL")

            linhaCruzada.put("estoque", mapaEstoque.getOrDefault(chaveCruzamento, null));
            linhaCruzada.put("vendas", mapaVendas.getOrDefault(chaveCruzamento, null));

            respostaFinal.add(linhaCruzada);
        }

        return respostaFinal;
    }
}