package br.fiap.projeto.pedido.adapter.gateway;

import br.fiap.projeto.pedido.usecase.port.messaging.IPedidoQueueAdapterGatewayOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

public class PedidoQueueAdapterGatewayOUT implements IPedidoQueueAdapterGatewayOUT {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RabbitTemplate rabbitTemplate;

    @Value("${pedido.pendente.queue}")
    private String pedidosPendentes;

    public PedidoQueueAdapterGatewayOUT(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String message) {
        this.rabbitTemplate.convertAndSend(pedidosPendentes, message);
        logger.info("Publicado na fila de pedidos pendentes!");
    }
}
