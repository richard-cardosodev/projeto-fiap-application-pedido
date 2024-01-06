package br.fiap.projeto.pedido.usecase.port.adaptergateway;

import br.fiap.projeto.pedido.entity.integration.PagamentoPedido;

import java.util.UUID;

public interface IPedidoPagamentoIntegrationAdapterGateway {
    PagamentoPedido buscaStatusPagamentoPorCodigoPedido(UUID codigoPedido);

    PagamentoPedido iniciaPagamento(UUID codigoPedido, Double valorTotal);
}
