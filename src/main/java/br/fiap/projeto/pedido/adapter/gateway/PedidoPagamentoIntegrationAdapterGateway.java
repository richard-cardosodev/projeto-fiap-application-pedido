package br.fiap.projeto.pedido.adapter.gateway;

import br.fiap.projeto.pedido.adapter.mapper.PagamentoMapper;
import br.fiap.projeto.pedido.entity.integration.PagamentoPedido;
import br.fiap.projeto.pedido.external.integration.PedidoPagamentoIntegration;
import br.fiap.projeto.pedido.external.integration.port.NovoPagamento;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoPagamentoIntegrationAdapterGateway;

import java.util.UUID;

public class PedidoPagamentoIntegrationAdapterGateway implements IPedidoPagamentoIntegrationAdapterGateway {
    final PedidoPagamentoIntegration pedidoPagamentoIntegration;

    public PedidoPagamentoIntegrationAdapterGateway(PedidoPagamentoIntegration pedidoPagamentoIntegration) {
        this.pedidoPagamentoIntegration = pedidoPagamentoIntegration;
    }

    @Override
    public PagamentoPedido buscaStatusPagamentoPorCodigoPedido(UUID codigoPedido) {
        return PagamentoMapper.toPagamentoPedido(pedidoPagamentoIntegration.buscaStatusPagamentoPorCodigoPedido(codigoPedido.toString()));
    }
    @Override
    public PagamentoPedido iniciaPagamento(UUID codigoPedido, Double valorTotal) {
        return PagamentoMapper.toPagamentoPedido(pedidoPagamentoIntegration.iniciaPagamento(new NovoPagamento(codigoPedido.toString(), valorTotal)));
    }

}
