package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.ProdutoEstoque;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoEstoqueRepository extends MongoRepository<ProdutoEstoque, String> {

    // Método essencial para o Autocomplete do Inventário de Novos pesquisar por Nome ou SKU
    List<ProdutoEstoque> findByItemContainingIgnoreCaseOrSkuContainingIgnoreCase(String item, String sku);

    List<ProdutoEstoque> findBySku(String sku);
}