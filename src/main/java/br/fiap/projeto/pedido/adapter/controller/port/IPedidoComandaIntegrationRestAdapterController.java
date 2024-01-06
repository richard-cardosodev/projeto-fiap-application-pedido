package br.fiap.projeto.pedido.adapter.controller.port;

import br.fiap.projeto.pedido.adapter.controller.rest.response.PedidoDTO;

import java.util.UUID;

public interface IPedidoComandaIntegrationRestAdapterController {
    PedidoDTO criaComanda(UUID codigoPedido) throws Exception;
}
