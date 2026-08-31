package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.RmaControle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RmaControleRepository extends MongoRepository<RmaControle, String> {
    Page<RmaControle> findAllByOrderByOrdemDesc(Pageable pageable);
    Optional<RmaControle> findTopByOrderByOrdemDesc();
}