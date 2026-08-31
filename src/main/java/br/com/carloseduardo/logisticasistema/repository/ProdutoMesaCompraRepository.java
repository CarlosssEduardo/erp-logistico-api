package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.ProdutoMesaCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoMesaCompraRepository extends MongoRepository<ProdutoMesaCompra, String> {

    Page<ProdutoMesaCompra> findByCategoriaAba(String categoriaAba, Pageable pageable);

    List<ProdutoMesaCompra> findByDescricaoLimpaAndCategoriaAba(String descricaoLimpa, String categoriaAba);

    @Query(value = "{ 'categoriaAba' : ?0 }", fields = "{ 'marca' : 1 }")
    List<ProdutoMesaCompra> findByCategoriaAba(String categoriaAba);

    default List<String> findDistinctMarcasByCategoriaAba(String categoriaAba) {
        return findByCategoriaAba(categoriaAba).stream()
                .map(ProdutoMesaCompra::getMarca)
                .filter(m -> m != null && !m.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    // 🔥 BUSCA BLINDADA E AVANÇADA: Pesquisa no Nome E em qualquer Código do Fornecedor!
    @Query("{ 'categoriaAba': ?0, " +
            "  $and: [ " +
            "    { $or: [ { 'marca': { $regex: ?1, $options: 'i' } }, { $expr: { $eq: [?1, ''] } } ] }, " +
            "    { $or: [ " +
            "       { 'descricaoLimpa': { $regex: ?2, $options: 'i' } }, " +
            "       { $expr: { $gt: [ { $size: { $filter: { input: { $objectToArray: '$codigosFornecedores' }, as: 'k', cond: { $regexMatch: { input: '$$k.v', regex: ?2, options: 'i' } } } } }, 0 ] } } " +
            "    ] } " +
            "  ] }")
    Page<ProdutoMesaCompra> findByFiltros(String categoriaAba, String marca, String busca, Pageable pageable);
}