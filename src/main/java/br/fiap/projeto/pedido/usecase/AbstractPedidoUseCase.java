package br.fiap.projeto.pedido.usecase;

import br.fiap.projeto.pedido.entity.Pedido;
import br.fiap.projeto.pedido.usecase.enums.MensagemErro;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoRepositoryAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.usecase.IAbstractPedidoUseCase;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractPedidoUseCase implements IAbstractPedidoUseCase {
    protected final IPedidoRepositoryAdapterGateway IPedidoRepositoryAdapterGateway;
    protected AbstractPedidoUseCase(IPedidoRepositoryAdapterGateway IPedidoRepositoryAdapterGateway) {
        this.IPedidoRepositoryAdapterGateway = IPedidoRepositoryAdapterGateway;
    }
    /**
     * Busca pedido pelo código e retorna um objeto de Pedido
     * Utilizar pora tratamentos internos somente para facilitar a busca minimizando a necessidade
     * de alterar o tipo de objeto
     * @param codigo - Codigo do pedido
     * @return retorna um objeto do tipo pedido da camada de domínio
     */
    protected Pedido buscar(UUID codigo) {
        Optional<Pedido> optionalPedido = IPedidoRepositoryAdapterGateway.buscaPedido(codigo);
        optionalPedido.orElseThrow(() -> new EntityNotFoundException(MensagemErro.PEDIDO_NOT_FOUND.getMessage()));
        return optionalPedido.get();
    }
    protected Boolean pedidoExists(UUID codigo) {
        try {
            buscar(codigo);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
