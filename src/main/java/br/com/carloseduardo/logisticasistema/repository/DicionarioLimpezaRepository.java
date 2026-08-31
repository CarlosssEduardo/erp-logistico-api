package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.DicionarioLimpeza;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DicionarioLimpezaRepository extends MongoRepository<DicionarioLimpeza, String> {
    // Verifica se a palavra já existe para não cadastrar duplicado
    boolean existsByPalavraRemoverIgnoreCase(String palavraRemover);
}