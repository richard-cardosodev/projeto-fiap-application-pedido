package br.fiap.projeto.pedido.usecase;

import br.fiap.projeto.pedido.entity.Pedido;
import br.fiap.projeto.pedido.entity.enums.StatusPedido;
import br.fiap.projeto.pedido.entity.integration.PagamentoPedido;
import br.fiap.projeto.pedido.usecase.enums.MensagemErro;
import br.fiap.projeto.pedido.usecase.exception.IntegrationPagamentoException;
import br.fiap.projeto.pedido.usecase.exception.InvalidStatusException;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoPagamentoIntegrationAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoRepositoryAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.usecase.IPedidoComandaIntegrationUseCase;
import br.fiap.projeto.pedido.usecase.port.usecase.IPedidoPagamentoIntegrationUseCase;
import br.fiap.projeto.pedido.usecase.port.usecase.IPedidoWorkFlowUseCase;

import java.util.UUID;

public class PedidoPagamentoIntegrationUseCase extends AbstractPedidoUseCase implements IPedidoPagamentoIntegrationUseCase {
    private final IPedidoPagamentoIntegrationAdapterGateway pedidoPagamentoIntegrationAdapterGateway;
    private final IPedidoWorkFlowUseCase pedidoWorkFlowUseCase;
    private final IPedidoComandaIntegrationUseCase pedidoComandaIntegrationUseCase;

    public PedidoPagamentoIntegrationUseCase(IPedidoRepositoryAdapterGateway pedidoRepositoryAdapterGateway,
                                             IPedidoPagamentoIntegrationAdapterGateway pedidoPagamentoIntegrationAdapterGateway,
                                             IPedidoWorkFlowUseCase pedidoWorkFlowUseCase,
                                             IPedidoComandaIntegrationUseCase iPedidoComandaIntegrationUseCase) {
        super(pedidoRepositoryAdapterGateway);
        this.pedidoPagamentoIntegrationAdapterGateway = pedidoPagamentoIntegrationAdapterGateway;
        this.pedidoWorkFlowUseCase = pedidoWorkFlowUseCase;
        this.pedidoComandaIntegrationUseCase = iPedidoComandaIntegrationUseCase;
    }

    @Override
    public Pedido atualizarPagamentoPedido(UUID codigoPedido) throws Exception {
        if(!pedidoExists(codigoPedido)){
            throw new Exception(MensagemErro.PEDIDO_NOT_FOUND.getMessage());
        }
        Pedido pedido = this.buscar(codigoPedido);
        if(!pedido.getStatus().equals(StatusPedido.RECEBIDO)){
            throw new InvalidStatusException(MensagemErro.INVALID_STATUS.getMessage());
        }
        PagamentoPedido pagamentoPedido;
        try {
            pagamentoPedido = pedidoPagamentoIntegrationAdapterGateway.buscaStatusPagamentoPorCodigoPedido(codigoPedido);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IntegrationPagamentoException(MensagemErro.PAGAMENTO_INTEGRATION_ERROR.getMessage() + " " + e.getMessage());
        }
        if(pagamentoPedido.isPago()){
            return pedidoWorkFlowUseCase.pagar(codigoPedido);
        }
        if(pagamentoPedido.isCanceled()){
            return pedidoWorkFlowUseCase.cancelar(codigoPedido);
        }

        throw new Exception(MensagemErro.PEDIDO_NOT_APPROVED.getMessage());
    }

    @Override
    public Pedido recebeRetornoPagamento(PagamentoPedido pagamentoPedido) throws Exception {
        if(pagamentoPedido == null) {
            throw new IntegrationPagamentoException("Retorno inválido!");
        }
        Pedido pedido = buscar(UUID.fromString(pagamentoPedido.getCodigoPedido()));
        if(pedido == null) {
            throw new Exception(MensagemErro.PEDIDO_NOT_FOUND.getMessage());
        }
        if(pagamentoPedido.isPago()){
            Pedido pedidoPago = pedidoWorkFlowUseCase.pagar(pedido.getCodigo());
            this.pedidoComandaIntegrationUseCase.criaComanda(pedidoPago.getCodigo());
            return pedidoPago;
        }
        if(pagamentoPedido.isCanceled()){
            return pedidoWorkFlowUseCase.cancelar(pedido.getCodigo());
        }

        throw new Exception(MensagemErro.PEDIDO_NOT_APPROVED.getMessage());
    }
}
