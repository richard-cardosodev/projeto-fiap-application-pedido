package br.fiap.projeto.pedido.adapter.gateway;

import br.fiap.projeto.pedido.entity.Pedido;
import br.fiap.projeto.pedido.usecase.exception.JsonProcessingException;
import br.fiap.projeto.pedido.usecase.port.IJsonConverter;
import br.fiap.projeto.pedido.usecase.port.messaging.IPedidoQueueAdapterGatewayOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

public class PedidoQueueAdapterGatewayOUT implements IPedidoQueueAdapterGatewayOUT {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IJsonConverter jsonConverter;

    private RabbitTemplate rabbitTemplate;

    @Value("${pedido.pendente.queue}")
    private String pedidosPendentes;

    public PedidoQueueAdapterGatewayOUT(RabbitTemplate rabbitTemplate, IJsonConverter jsonConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void publish(Pedido pedido) throws JsonProcessingException {
        this.rabbitTemplate.convertAndSend(pedidosPendentes, jsonConverter.convertObjectToJsonString(pedido));
        logger.info("Publicado na fila de pedidos pendentes!");
    }
}
