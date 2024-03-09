package br.fiap.projeto.pedido.usecase.port.messaging;

import br.fiap.projeto.pedido.entity.Pedido;
import br.fiap.projeto.pedido.usecase.exception.JsonProcessingException;

public interface IPedidoQueueAdapterGatewayOUT {
    void publish(Pedido pedido) throws JsonProcessingException;
}
