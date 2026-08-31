package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.FornecedorCredito;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FornecedorCreditoRepository extends MongoRepository<FornecedorCredito, String> {}