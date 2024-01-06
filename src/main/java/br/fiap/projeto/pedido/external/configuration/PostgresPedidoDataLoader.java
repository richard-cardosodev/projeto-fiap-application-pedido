package br.fiap.projeto.pedido.external.configuration;

import br.fiap.projeto.pedido.adapter.gateway.PedidoRepositoryAdapterGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgresPedidoDataLoader {
    @Autowired
    private PedidoRepositoryAdapterGateway pedidoRepository;

}