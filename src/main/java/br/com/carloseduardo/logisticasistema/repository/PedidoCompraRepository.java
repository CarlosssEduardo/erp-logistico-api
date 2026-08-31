package br.com.carloseduardo.logisticasistema.repository;

import br.com.carloseduardo.logisticasistema.model.PedidoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoCompraRepository extends MongoRepository<PedidoCompra, String> {

    // 🔥 CORREÇÃO: Atualizamos as buscas para baterem com as novas colunas!
    List<PedidoCompra> findByStatusPedido(String statusPedido);
    List<PedidoCompra> findByStatusLogistica(String statusLogistica);
    List<PedidoCompra> findByStatusPagamento(String statusPagamento);

    // Para listar na tela de histórico de PDCs com paginação
    Page<PedidoCompra> findAllByOrderByDataPedidoDesc(Pageable pageable);

    // Mudou de Optional para List para aceitar PDCs repetidos na planilha
    List<PedidoCompra> findByNumeroPdc(String numeroPdc);

    List<PedidoCompra> findAllByOrderByOrdemAsc();

    // Puxa paginado ordenando do maior pro menor (76, 75, 74...) para os novos ficarem no topo
    Page<PedidoCompra> findAllByOrderByOrdemDesc(Pageable pageable);

    // Busca qual foi o último número de ordem cadastrado no banco (Ex: 75)
    Optional<PedidoCompra> findTopByOrderByOrdemDesc();
}