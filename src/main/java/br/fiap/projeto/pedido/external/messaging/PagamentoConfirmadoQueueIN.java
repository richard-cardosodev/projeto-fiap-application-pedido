package br.fiap.projeto.pedido.external.messaging;

import br.fiap.projeto.pedido.adapter.controller.port.IPedidoPagamentoIntegrationRestAdapterController;
import br.fiap.projeto.pedido.entity.enums.StatusPagamento;
import br.fiap.projeto.pedido.external.integration.port.Pagamento;
import br.fiap.projeto.pedido.usecase.exception.JsonProcessingException;
import br.fiap.projeto.pedido.usecase.port.IJsonConverter;
import br.fiap.projeto.pedido.usecase.port.messaging.IPagamentoConfirmadoQueueIN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Service
public class PagamentoConfirmadoQueueIN implements IPagamentoConfirmadoQueueIN {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IJsonConverter jsonConverter;
    private final IPedidoPagamentoIntegrationRestAdapterController pedidoPagamentoIntegrationRestAdapterController;

    public PagamentoConfirmadoQueueIN(IJsonConverter jsonConverter, IPedidoPagamentoIntegrationRestAdapterController pedidoPagamentoIntegrationRestAdapterController) {
        this.jsonConverter = jsonConverter;
        this.pedidoPagamentoIntegrationRestAdapterController = pedidoPagamentoIntegrationRestAdapterController;
    }

    @Transactional
    @RabbitListener(queues = {"${pagamento.confirmado.queue}"})
    @Override
    public void receive(String message) {
        try {
            Map<String, Object> messageMap = jsonConverter.stringJsonToMapStringObject(message);
            Pagamento pagamento = new Pagamento((String) messageMap.get("codigoPedido"), new Date((Long) messageMap.get("dataPagamento")), StatusPagamento.valueOf((String) messageMap.get("status")));
            this.pedidoPagamentoIntegrationRestAdapterController.recebeRetornoPagamento(pagamento);
            logger.info("Processando retorno pagamento confirmado");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
