package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.VendaMensalResumo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendaMensalResumoRepository extends MongoRepository<VendaMensalResumo, String> {
    Optional<VendaMensalResumo> findByMesReferencia(String mesReferencia);
}