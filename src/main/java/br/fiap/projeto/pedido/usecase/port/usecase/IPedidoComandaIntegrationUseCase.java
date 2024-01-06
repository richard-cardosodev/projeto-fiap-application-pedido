package br.fiap.projeto.pedido.usecase.port.usecase;

import br.fiap.projeto.pedido.entity.Pedido;

import java.util.UUID;

public interface IPedidoComandaIntegrationUseCase {
    Pedido criaComanda(UUID codigoPedido) throws Exception;
}
