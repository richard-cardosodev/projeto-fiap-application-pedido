package br.fiap.projeto.pedido.adapter.gateway;

import br.fiap.projeto.pedido.adapter.mapper.ProdutoMapper;
import br.fiap.projeto.pedido.entity.integration.ProdutoPedido;
import br.fiap.projeto.pedido.external.integration.PedidoProdutoIntegration;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoProdutoIntegrationAdapterGateway;

import java.util.UUID;

public class PedidoProdutoIntegrationAdapterGateway implements IPedidoProdutoIntegrationAdapterGateway {
    private final PedidoProdutoIntegration pedidoProdutoIntegration;

    public PedidoProdutoIntegrationAdapterGateway(PedidoProdutoIntegration pedidoProdutoIntegration) {
        this.pedidoProdutoIntegration = pedidoProdutoIntegration;
    }
    @Override
    public ProdutoPedido getProduto(UUID codigoProduto) {
        return ProdutoMapper.toProdutoPedido(pedidoProdutoIntegration.getProduto(codigoProduto));
    }
}
