package br.fiap.projeto.pedido.external.repository.postgres;

import br.fiap.projeto.pedido.entity.enums.StatusPedido;
import br.fiap.projeto.pedido.external.repository.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringPedidoRepository extends JpaRepository<PedidoEntity, UUID> {
    Optional<PedidoEntity> findByCodigo(UUID codigo);
    void deleteByCodigo(UUID codigo);
    List<PedidoEntity> findByStatusEquals(StatusPedido statusPedido);
    List<PedidoEntity> findByStatusIn(List<StatusPedido> statuses);
}