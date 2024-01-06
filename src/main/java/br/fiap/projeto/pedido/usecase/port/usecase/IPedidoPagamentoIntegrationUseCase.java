package br.fiap.projeto.pedido.usecase.port.usecase;

import br.fiap.projeto.pedido.entity.Pedido;
import br.fiap.projeto.pedido.entity.integration.PagamentoPedido;

import java.util.UUID;

public interface IPedidoPagamentoIntegrationUseCase {
    Pedido atualizarPagamentoPedido(UUID codigoPedido) throws Exception;

    Pedido recebeRetornoPagamento(PagamentoPedido pagamentoPedido) throws Exception;
}
