package br.fiap.projeto.pedido.external.configuration;

import br.fiap.projeto.pedido.adapter.controller.*;
import br.fiap.projeto.pedido.adapter.controller.port.*;
import br.fiap.projeto.pedido.adapter.gateway.*;
import br.fiap.projeto.pedido.external.integration.PedidoClienteIntegration;
import br.fiap.projeto.pedido.external.integration.PedidoComandaIntegration;
import br.fiap.projeto.pedido.external.integration.PedidoPagamentoIntegration;
import br.fiap.projeto.pedido.external.integration.PedidoProdutoIntegration;
import br.fiap.projeto.pedido.external.repository.postgres.SpringPedidoRepository;
import br.fiap.projeto.pedido.usecase.*;
import br.fiap.projeto.pedido.usecase.port.IJsonConverter;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.*;
import br.fiap.projeto.pedido.usecase.port.messaging.IPedidoQueueAdapterGatewayOUT;
import br.fiap.projeto.pedido.usecase.port.usecase.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanPedidoConfiguration {
    @Bean
    IPedidoManagementUseCase pedidoManagementUseCase(IPedidoRepositoryAdapterGateway pedidoRepositoryAdapterGateway,
                                                     IPedidoProdutoIntegrationAdapterGateway pedidoProdutoIntegrationAdapterGateway,
                                                     IPedidoClienteIntegrationAdapterGateway pedidoClienteIntegration){
        return new PedidoManagementUseCase(pedidoRepositoryAdapterGateway,
                pedidoProdutoIntegrationAdapterGateway,
                pedidoClienteIntegration);
    }
    @Bean
    IPedidoQueryUseCase pedidoQueryUseCase(IPedidoRepositoryAdapterGateway pedidoRepositoryAdapterGateway){
        return new PedidoQueryUseCase(pedidoRepositoryAdapterGateway);
    }
    @Bean
    IPedidoWorkFlowUseCase pedidoWorkFlowUseCase(IPedidoRepositoryAdapterGateway pedidoRepositoryAdapterGateway,
                                                 IPedidoQueueAdapterGatewayOUT pedidoQueueAdapterGatewayOUT,
                                                 IJsonConverter jsonConverter){
        return new PedidoWorkFlowUseCase(pedidoRepositoryAdapterGateway,
                pedidoQueueAdapterGatewayOUT,
                jsonConverter);
    }
    @Bean
    IPedidoComandaIntegrationUseCase pedidoComandaIntegrationUseCase(IPedidoComandaIntegrationAdapterGateway pedidoComandaIntegrationAdapterGateway,
                                                                     IPedidoWorkFlowUseCase pedidoWorkFlowUseCase){
        return new PedidoComandaIntegrationUseCase(pedidoComandaIntegrationAdapterGateway,
                pedidoWorkFlowUseCase);
    }
    @Bean
    IPedidoPagamentoIntegrationUseCase pedidoPagamentoIntegrationUseCase(IPedidoRepositoryAdapterGateway pedidoRepositoryAdapterGateway,
                                                                         IPedidoPagamentoIntegrationAdapterGateway pedidoPagamentoIntegrationAdapterGateway,
                                                                         IPedidoWorkFlowUseCase pedidoWorkFlowUseCase,
                                                                         IPedidoComandaIntegrationUseCase pedidoComandaIntegrationUseCase){
        return new PedidoPagamentoIntegrationUseCase(pedidoRepositoryAdapterGateway,
                pedidoPagamentoIntegrationAdapterGateway,
                pedidoWorkFlowUseCase,
                pedidoComandaIntegrationUseCase);
    }
    @Bean
    IPedidoManagementRestAdapterController pedidoManagementRestAdapterController(IPedidoManagementUseCase pedidoUseCase){
        return new PedidoManagementRestAdapterController(pedidoUseCase);
    }
    @Bean
    IPedidoQueryRestAdapterController pedidoQueryRestAdapterController(IPedidoQueryUseCase pedidoQueryUseCase){
        return new PedidoQueryRestAdapterController(pedidoQueryUseCase);
    }
    @Bean
    IPedidoWorkFlowRestAdapterController pedidoWorkFlowRestAdapterController(IPedidoWorkFlowUseCase PedidoWorkFlowUseCase){
        return new PedidoWorkFlowRestAdapterController(PedidoWorkFlowUseCase);
    }
    @Bean
    IPedidoComandaIntegrationRestAdapterController pedidoComandaIntegrationRestAdapterController(IPedidoComandaIntegrationUseCase pedidoComandaIntegrationUseCase){
        return new PedidoComandaIntegrationRestAdapterController(pedidoComandaIntegrationUseCase);
    }
    @Bean
    IPedidoPagamentoIntegrationRestAdapterController pedidoPagamentoIntegrationRestAdapterController(IPedidoPagamentoIntegrationUseCase pedidoPagamentoIntegrationUseCase){
        return new PedidoPagamentoIntegrationRestAdapterController(pedidoPagamentoIntegrationUseCase);
    }
    @Bean
    IPedidoRepositoryAdapterGateway pedidoAdapterGateway(SpringPedidoRepository springPedidoRepository){
        return new PedidoRepositoryAdapterGateway(springPedidoRepository);
    }
    @Bean
    IPedidoProdutoIntegrationAdapterGateway pedidoProdutoIntegrationAdapterGateway(PedidoProdutoIntegration pedidoProdutoIntegration){
        return new PedidoProdutoIntegrationAdapterGateway(pedidoProdutoIntegration);
    }
    @Bean
    IPedidoClienteIntegrationAdapterGateway pedidoClienteIntegrationAdapterGateway(PedidoClienteIntegration pedidoClienteIntegration){
        return new PedidoClienteIntegrationAdapterGateway(pedidoClienteIntegration);
    }
    @Bean
    IPedidoComandaIntegrationAdapterGateway pedidoComandaIntegrationAdapterGateway(PedidoComandaIntegration pedidoComandaIntegration){
        return new PedidoComandaIntegrationAdapterGateway(pedidoComandaIntegration);
    }
    @Bean
    IPedidoPagamentoIntegrationAdapterGateway pedidoPagamentoIntegrationAdapterGateway(PedidoPagamentoIntegration pagamentoIntegration){
        return new PedidoPagamentoIntegrationAdapterGateway(pagamentoIntegration);
    }
    @Bean
    IPedidoQueueAdapterGatewayOUT pedidoQueueAdapterGatewayOUT(RabbitTemplate rabbitTemplate) {
        return new PedidoQueueAdapterGatewayOUT(rabbitTemplate);
    }
}