package br.fiap.projeto.pedido.usecase.port.adaptergateway;

import br.fiap.projeto.pedido.entity.integration.ProdutoPedido;

import java.util.UUID;

public interface IPedidoProdutoIntegrationAdapterGateway {
    ProdutoPedido getProduto(UUID codigoProduto);
}
