package br.fiap.projeto.pedido.adapter.gateway;

import br.fiap.projeto.pedido.external.integration.PedidoClienteIntegration;
import br.fiap.projeto.pedido.external.integration.port.Cliente;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoClienteIntegrationAdapterGateway;

import java.util.UUID;

public class PedidoClienteIntegrationAdapterGateway implements IPedidoClienteIntegrationAdapterGateway {
    private final PedidoClienteIntegration pedidoClienteIntegration;

    public PedidoClienteIntegrationAdapterGateway(PedidoClienteIntegration pedidoClienteIntegration) {
        this.pedidoClienteIntegration = pedidoClienteIntegration;
    }

    @Override
    public Boolean verificaClienteExiste(UUID codigoCliente) {
        Cliente cliente = pedidoClienteIntegration.busca(codigoCliente.toString());
        return cliente != null && !cliente.getCodigo().isEmpty();
    }
}