package br.fiap.projeto.pedido.usecase.port.adaptergateway;

import java.util.UUID;

public interface IPedidoClienteIntegrationAdapterGateway {
    Boolean verificaClienteExiste(UUID codigoCliente);
}
