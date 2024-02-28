package br.fiap.projeto.pedido.usecase.port.messaging;

public interface IPedidoQueueAdapterGatewayOUT {
    void publish(String message);
}
