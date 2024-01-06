package br.fiap.projeto.pedido.adapter.gateway;

import br.fiap.projeto.pedido.adapter.mapper.ComandaMapper;
import br.fiap.projeto.pedido.entity.integration.ComandaPedido;
import br.fiap.projeto.pedido.external.integration.PedidoComandaIntegration;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoComandaIntegrationAdapterGateway;

import java.util.UUID;

public class PedidoComandaIntegrationAdapterGateway implements IPedidoComandaIntegrationAdapterGateway {
    private final PedidoComandaIntegration pedidoComandaIntegration;

    public PedidoComandaIntegrationAdapterGateway(PedidoComandaIntegration pedidoComandaIntegration) {
        this.pedidoComandaIntegration = pedidoComandaIntegration;
    }

    @Override
    public ComandaPedido criaComanda(UUID codigoPedido) {
        return ComandaMapper.toComandaPedido(
                pedidoComandaIntegration.criaComanda(
                        ComandaMapper.toCriaComanda(codigoPedido)));
    }
}
