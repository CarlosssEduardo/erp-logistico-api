package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.VendaVendedorResumo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VendaVendedorRepository extends MongoRepository<VendaVendedorResumo, String> {
    List<VendaVendedorResumo> findByMesReferencia(String mesReferencia);
}