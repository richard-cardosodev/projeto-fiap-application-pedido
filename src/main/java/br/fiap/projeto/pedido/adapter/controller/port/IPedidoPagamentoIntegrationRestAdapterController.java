package br.fiap.projeto.pedido.adapter.controller.port;

import br.fiap.projeto.pedido.adapter.controller.rest.response.PedidoDTO;
import br.fiap.projeto.pedido.external.integration.port.Pagamento;

import java.util.UUID;

public interface IPedidoPagamentoIntegrationRestAdapterController {
    PedidoDTO atualizarPagamentoPedido(UUID codigoPedido) throws Exception;

    void recebeRetornoPagamento(Pagamento retornoPagamento) throws Exception;
}
