package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.FornecedorItemNovo;
import br.com.carloseduardo.logisticasistema.model.ProdutoEstoque;
import br.com.carloseduardo.logisticasistema.repository.FornecedorItemNovoRepository;
import br.com.carloseduardo.logisticasistema.repository.ProdutoEstoqueRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FornecedorItemNovoService {

    @Autowired
    private FornecedorItemNovoRepository repository;

    // 🔥 Injetamos o repositório de Estoque para criar o produto ao vincular
    @Autowired
    private ProdutoEstoqueRepository estoqueRepository;

    public void processarPlanilhaFornecedor(MultipartFile file, String nomeFornecedor) throws Exception {
        List<FornecedorItemNovo> itensParaSalvar = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Pula cabeçalho

                String codigo = getValorTexto(row.getCell(0));
                if (codigo == null || codigo.trim().isEmpty()) continue;

                FornecedorItemNovo item = new FornecedorItemNovo();
                item.setFornecedor(nomeFornecedor != null ? nomeFornecedor.toUpperCase() : "FORNECEDOR");
                item.setCodigoFornecedor(codigo);
                item.setDescricaoOriginal(getValorTexto(row.getCell(2))); // Coluna Modelo/Descrição
                item.setValor(getValorNumerico(row.getCell(3)));          // Coluna Valor
                item.setQuantidade((int) getValorNumerico(row.getCell(4))); // Quantidade
                item.setVinculado(false);

                itensParaSalvar.add(item);
            }

            repository.saveAll(itensParaSalvar);
        }
    }

    public void vincularProduto(String idItem, String skuUniversal, String nomeUniversal) {
        // 1. Encontra e marca o item pendente como vinculado
        FornecedorItemNovo item = repository.findById(idItem).orElseThrow(() -> new RuntimeException("Item não encontrado"));
        item.setSkuUniversal(skuUniversal);
        item.setProdutoUniversalNome(nomeUniversal);
        item.setVinculado(true);
        repository.save(item);

        // 2. Cria dinamicamente o novo produto no Estoque Bruto
        ProdutoEstoque novoProduto = new ProdutoEstoque();
        novoProduto.setSku(item.getCodigoFornecedor());
        novoProduto.setCategoria("TELA"); // Podendo ser parametrizado no futuro
        novoProduto.setMarca(item.getFornecedor());
        // Monta o nome: Nome do fornecedor + Nome Universal
        novoProduto.setItem(item.getDescricaoOriginal() + " (" + nomeUniversal + ")");
        novoProduto.setQuantidade(item.getQuantidade());
        novoProduto.setValorUnitario(item.getValor());
        novoProduto.setValorTotal(item.getQuantidade() * item.getValor());
        novoProduto.setClassificado(true);
        novoProduto.setNaMesaDeCompras(false);

        // A chave de ouro para a Mesa de Compras cruzar os dados:
        novoProduto.setSkuMestre(skuUniversal);

        estoqueRepository.save(novoProduto);
    }

    private String getValorTexto(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf(cell.getNumericCellValue());
        return "";
    }

    private double getValorNumerico(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().replace("R$", "").replace(",", ".").trim());
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}