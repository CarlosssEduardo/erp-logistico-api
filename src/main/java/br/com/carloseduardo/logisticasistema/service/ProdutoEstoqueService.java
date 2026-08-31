package br.com.carloseduardo.logisticasistema.service;

import br.com.carloseduardo.logisticasistema.model.ProdutoEstoque;
import br.com.carloseduardo.logisticasistema.repository.ProdutoEstoqueRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProdutoEstoqueService {

    @Autowired
    private ProdutoEstoqueRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void processarPlanilhaBalanco(MultipartFile file) throws Exception {
        repository.deleteAll(); // Limpa e insere fielmente o balanço novo

        List<ProdutoEstoque> produtosParaSalvar = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String sku = getValorTexto(row.getCell(1));
                if (sku == null || sku.trim().isEmpty()) continue;

                ProdutoEstoque produto = new ProdutoEstoque();
                produto.setSku(sku);
                produto.setNaMesaDeCompras(false);

                produto.setCategoria(getValorTexto(row.getCell(0)));
                produto.setMarca(getValorTexto(row.getCell(2)));

                String nomeItem = getValorTexto(row.getCell(3));
                produto.setItem(nomeItem);

                produto.setUsadoNosModelos(getValorTexto(row.getCell(4)));
                produto.setLocalizacao(getValorTexto(row.getCell(5)));

                int qtd = getValorInteiro(row.getCell(6));
                produto.setQuantidade(qtd);

                produto.setUnidade(getValorTexto(row.getCell(7)));
                produto.setValorUnitario(getValorNumerico(row.getCell(8)));
                produto.setValorTotal(getValorNumerico(row.getCell(9)));
                produto.setCodBarras(getValorTexto(row.getCell(10)));

                if (nomeItem != null) {
                    String itemUpper = nomeItem.toUpperCase();
                    if (itemUpper.contains("TELA") || itemUpper.contains("BATERIA") ||
                            itemUpper.contains("FLEX") || itemUpper.contains("COMPONENTE")) {
                        produto.setClassificado(true);
                    } else {
                        produto.setClassificado(false);
                    }
                }

                produtosParaSalvar.add(produto);
            }

            repository.saveAll(produtosParaSalvar);
        }
    }

    public Page<ProdutoEstoque> listarEstoqueFiltrado(String busca, String categoria, String marca, Boolean isRma, Pageable pageable) {
        Query query = construirQueryFiltros(busca, categoria, marca, isRma);
        long count = mongoTemplate.count(query, ProdutoEstoque.class);
        query.with(pageable);
        List<ProdutoEstoque> itens = mongoTemplate.find(query, ProdutoEstoque.class);
        return new PageImpl<>(itens, pageable, count);
    }

    public Map<String, Object> calcularTotaisGlobais(String busca, String categoria, String marca, Boolean isRma) {
        Query query = construirQueryFiltros(busca, categoria, marca, isRma);
        List<ProdutoEstoque> todosFiltrados = mongoTemplate.find(query, ProdutoEstoque.class);

        double totalInvestimento = 0;
        double totalRma = 0;
        int itensFisicos = 0;

        for (ProdutoEstoque p : todosFiltrados) {
            int qtd = p.getQuantidade() != null ? p.getQuantidade() : 0;
            double vUnit = p.getValorUnitario() != null ? p.getValorUnitario() : 0;
            double totalLinha = vUnit * qtd;

            if (qtd < 0) {
                totalRma += Math.abs(totalLinha);
            } else {
                totalInvestimento += totalLinha;
            }
            itensFisicos += qtd;
        }

        return Map.of(
                "totalInvestimento", totalInvestimento,
                "totalRma", totalRma,
                "itensFisicos", itensFisicos
        );
    }

    public List<String> buscarCategoriasUnicas() {
        return mongoTemplate.findDistinct(new Query(), "categoria", ProdutoEstoque.class, String.class)
                .stream().filter(c -> c != null && !c.trim().isEmpty()).sorted().collect(Collectors.toList());
    }

    public List<String> buscarMarcasUnicas() {
        return mongoTemplate.findDistinct(new Query(), "marca", ProdutoEstoque.class, String.class)
                .stream().filter(m -> m != null && !m.trim().isEmpty()).sorted().collect(Collectors.toList());
    }

    private Query construirQueryFiltros(String busca, String categoria, String marca, Boolean isRma) {
        Query query = new Query();
        if (busca != null && !busca.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("sku").regex(busca, "i"),
                    Criteria.where("item").regex(busca, "i")
            ));
        }
        if (categoria != null && !categoria.isEmpty() && !categoria.equals("Todas as Categorias")) {
            query.addCriteria(Criteria.where("categoria").regex(categoria, "i"));
        }
        if (marca != null && !marca.isEmpty() && !marca.equals("Todas as Marcas")) {
            query.addCriteria(Criteria.where("marca").regex(marca, "i"));
        }
        if (isRma != null && isRma) {
            query.addCriteria(Criteria.where("quantidade").lt(0));
        }
        return query;
    }

    private String getValorTexto(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().replaceAll("[\\uFEFF\\u200B]", "").trim();
        }
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