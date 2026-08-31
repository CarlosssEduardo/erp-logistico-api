package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.FornecedorItemNovo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FornecedorItemNovoRepository extends MongoRepository<FornecedorItemNovo, String> {
    List<FornecedorItemNovo> findByFornecedorAndVinculado(String fornecedor, boolean vinculado);
    List<FornecedorItemNovo> findByVinculado(boolean vinculado);
}