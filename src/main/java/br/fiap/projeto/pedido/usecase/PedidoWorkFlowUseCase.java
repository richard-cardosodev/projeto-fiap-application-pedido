package br.fiap.projeto.pedido.usecase;

import br.fiap.projeto.pedido.entity.Pedido;
import br.fiap.projeto.pedido.entity.enums.StatusPedido;
import br.fiap.projeto.pedido.usecase.enums.MensagemErro;
import br.fiap.projeto.pedido.usecase.exception.InvalidStatusException;
import br.fiap.projeto.pedido.usecase.exception.NoItensException;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoRepositoryAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.messaging.IPedidoQueueAdapterGatewayOUT;
import br.fiap.projeto.pedido.usecase.port.usecase.IPedidoWorkFlowUseCase;

import java.util.UUID;

public class PedidoWorkFlowUseCase extends AbstractPedidoUseCase  implements IPedidoWorkFlowUseCase {
    private final IPedidoQueueAdapterGatewayOUT pedidoQueueAdapterGatewayOUT;

    public PedidoWorkFlowUseCase(IPedidoRepositoryAdapterGateway IPedidoRepositoryAdapterGateway,
                                 IPedidoQueueAdapterGatewayOUT pedidoQueueAdapterGatewayOUT) {
        super(IPedidoRepositoryAdapterGateway);
        this.pedidoQueueAdapterGatewayOUT = pedidoQueueAdapterGatewayOUT;
    }
    @Override
    public Pedido receber(UUID codigo) throws Exception {
        Pedido pedido = this.buscar(codigo);
        if(pedido.getStatus().equals(StatusPedido.INICIADO)){
            if(pedido.getItens().isEmpty()){
                throw new NoItensException(codigo.toString());
            }else {
                pedido.atualizarStatus(StatusPedido.RECEBIDO);
            }
        }else{
            throw new InvalidStatusException(MensagemErro.INVALID_STATUS.getMessage());
        }
        pedido = IPedidoRepositoryAdapterGateway.salvar(pedido);

        iniciaPagamento(pedido);

        return pedido;
    }
    @Override
    public Pedido pagar(UUID codigo) throws Exception {
        Pedido pedido = this.buscar(codigo);
        if(pedido.getStatus().equals(StatusPedido.RECEBIDO)){
            pedido.atualizarStatus(StatusPedido.PAGO);
        }else{
            throw new InvalidStatusException(MensagemErro.INVALID_STATUS.getMessage());
        }
        return IPedidoRepositoryAdapterGateway.salvar(pedido);
    }
    @Override
    public Pedido preparar(UUID codigo) throws Exception {
        Pedido pedido = this.buscar(codigo);
        if(pedido.getStatus().equals(StatusPedido.PAGO)){
            pedido.atualizarStatus(StatusPedido.EM_PREPARACAO);
        }else{
            throw new InvalidStatusException(MensagemErro.INVALID_STATUS.getMessage());
        }
        return IPedidoRepositoryAdapterGateway.salvar(pedido);
    }
    @Override
    public Pedido prontificar(UUID codigo) throws Exception {
        Pedido pedido = this.buscar(codigo);
        if(pedido.getStatus().equals(StatusPedido.EM_PREPARACAO)){
            pedido.atualizarStatus(StatusPedido.PRONTO);
        }else{
            throw new InvalidStatusException(MensagemErro.INVALID_STATUS.getMessage());
        }
        return IPedidoRepositoryAdapterGateway.salvar(pedido);
    }
    @Override
    public Pedido finalizar(UUID codigo) throws Exception {
        Pedido pedido = this.buscar(codigo);
        if(pedido.getStatus().equals(StatusPedido.PRONTO)){
            pedido.atualizarStatus(StatusPedido.FINALIZADO);
        }else{
            throw new InvalidStatusException(MensagemErro.INVALID_STATUS.getMessage());
        }
        return IPedidoRepositoryAdapterGateway.salvar(pedido);
    }

    @Override
    public Pedido cancelar(UUID codigo) throws Exception {
        Pedido pedido = this.buscar(codigo);
        pedido.atualizarStatus(StatusPedido.CANCELADO);
        return IPedidoRepositoryAdapterGateway.salvar(pedido);
    }
    private void iniciaPagamento(Pedido pedido) throws Exception {
        if(!pedido.getStatus().equals(StatusPedido.RECEBIDO)){
            throw new Exception(MensagemErro.INVALID_STATUS.getMessage());
        }
        pedidoQueueAdapterGatewayOUT.publish(pedido);
    }
}
