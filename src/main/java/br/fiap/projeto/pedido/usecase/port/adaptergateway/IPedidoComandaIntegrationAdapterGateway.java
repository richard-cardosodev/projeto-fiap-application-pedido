package br.fiap.projeto.pedido.usecase.port.adaptergateway;

import br.fiap.projeto.pedido.entity.integration.ComandaPedido;

import java.util.UUID;

public interface IPedidoComandaIntegrationAdapterGateway {
    ComandaPedido criaComanda(UUID codigoProduto);
}
