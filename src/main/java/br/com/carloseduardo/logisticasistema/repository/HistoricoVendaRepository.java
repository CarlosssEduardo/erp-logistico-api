package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.HistoricoVenda;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoVendaRepository extends MongoRepository<HistoricoVenda, String> {

    // Busca o histórico de um SKU específico na hora que a mesa de compras pedir
    HistoricoVenda findBySku(String sku);
}