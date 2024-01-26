package br.fiap.projeto.pedido.usecase;

import br.fiap.projeto.pedido.entity.ItemPedido;
import br.fiap.projeto.pedido.entity.Pedido;
import br.fiap.projeto.pedido.entity.enums.OperacaoProduto;
import br.fiap.projeto.pedido.entity.integration.ProdutoPedido;
import br.fiap.projeto.pedido.usecase.enums.MensagemErro;
import br.fiap.projeto.pedido.usecase.exception.IntegrationProdutoException;
import br.fiap.projeto.pedido.usecase.exception.InvalidOperacaoProdutoException;
import br.fiap.projeto.pedido.usecase.exception.InvalidStatusException;
import br.fiap.projeto.pedido.usecase.exception.ItemNotFoundException;
import br.fiap.projeto.pedido.usecase.exception.NoItensException;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoClienteIntegrationAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoProdutoIntegrationAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoRepositoryAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.usecase.IPedidoManagementUseCase;
import org.hibernate.ObjectNotFoundException;

import java.util.List;
import java.util.UUID;

public class PedidoManagementUseCase extends AbstractPedidoUseCase implements IPedidoManagementUseCase {
    private final IPedidoProdutoIntegrationAdapterGateway pedidoProdutoIntegrationAdapterGateway;
    private final IPedidoClienteIntegrationAdapterGateway pedidoClienteIntegrationAdapterGateway;

    public PedidoManagementUseCase(IPedidoRepositoryAdapterGateway IPedidoRepositoryAdapterGateway,
                                   IPedidoProdutoIntegrationAdapterGateway pedidoProdutoIntegrationAdapterGateway,
                                   IPedidoClienteIntegrationAdapterGateway pedidoClienteIntegrationAdapterGateway) {
        super(IPedidoRepositoryAdapterGateway);
        this.pedidoProdutoIntegrationAdapterGateway = pedidoProdutoIntegrationAdapterGateway;
        this.pedidoClienteIntegrationAdapterGateway = pedidoClienteIntegrationAdapterGateway;
    }

    @Override
    public Pedido criaPedido(String codigoCliente) throws InvalidStatusException, NoItensException {
        if (codigoCliente != null &&
                !codigoCliente.isEmpty() &&
                !pedidoClienteIntegrationAdapterGateway.verificaClienteExiste(UUID.fromString(codigoCliente))) {
            throw new ObjectNotFoundException(codigoCliente, "cliente");
        }
        return IPedidoRepositoryAdapterGateway.salvar(new Pedido(codigoCliente));
    }

    @Override
    public Pedido adicionarProduto(UUID codigoPedido, UUID codigoProduto)
            throws InvalidOperacaoProdutoException, ItemNotFoundException, IntegrationProdutoException,
            InvalidStatusException, NoItensException {
        ProdutoPedido produtoPedido = getProduto(codigoProduto);
        if (produtoPedido != null) {
            Pedido pedido = this.buscar(codigoPedido);
            if (this.isProdutoExisteNoPedido(codigoProduto, pedido)) {
                throw new InvalidOperacaoProdutoException(
                        MensagemErro.INVALID_OPERATION.getMessage() + " "
                                + MensagemErro.PRODUTO_EXIST_IN_THE_ORDER.getMessage());
            }
            ItemPedido itemPedido = new ItemPedido(codigoPedido,
                    codigoProduto,
                    pedido,
                    1,
                    produtoPedido.getNome(),
                    produtoPedido.getDescricao(),
                    produtoPedido.getPreco(),
                    produtoPedido.getCategoria(),
                    produtoPedido.getImagem(),
                    produtoPedido.getTempoPreparoMin());
            pedido.adicionarItem(itemPedido);
            this.atualizaValorTotal(pedido, itemPedido, OperacaoProduto.ADICIONAR);
            return IPedidoRepositoryAdapterGateway.salvar(pedido);
        } else {
            throw new ItemNotFoundException(MensagemErro.PRODUTO_NOT_FOUND.getMessage());
        }
    }

    @Override
    public Pedido removerProduto(UUID codigoPedido, UUID codigoProduto)
            throws InvalidOperacaoProdutoException, ItemNotFoundException, InvalidStatusException, NoItensException {
        Pedido pedido = this.buscar(codigoPedido);
        ItemPedido itemPedido = this.getItemPedidoByProduto(codigoProduto, pedido.getItens());
        if (itemPedido == null) {
            throw new ItemNotFoundException(MensagemErro.PRODUTO_NOT_FOUND.getMessage());
        }
        this.atualizaValorTotal(pedido, itemPedido, OperacaoProduto.REMOVER);
        pedido.getItens().remove(itemPedido);
        return IPedidoRepositoryAdapterGateway.salvar(pedido);
    }

    @Override
    public Pedido aumentarQuantidade(UUID codigoPedido, UUID codigoProduto)
            throws ItemNotFoundException, InvalidOperacaoProdutoException, InvalidStatusException, NoItensException {
        Pedido pedido = this.buscar(codigoPedido);
        ItemPedido itemPedido = this.getItemPedidoByProduto(codigoProduto, pedido.getItens());
        if (itemPedido == null) {
            throw new ItemNotFoundException(MensagemErro.ITEM_NOT_FOUND_IN_LIST.getMessage());
        }
        itemPedido.adicionarQuantidade();
        this.atualizaValorTotal(pedido, itemPedido, OperacaoProduto.ADICIONAR);
        return IPedidoRepositoryAdapterGateway.salvar(pedido);
    }

    @Override
    public Pedido reduzirQuantidade(UUID codigoPedido, UUID codigoProduto)
            throws ItemNotFoundException, InvalidOperacaoProdutoException, InvalidStatusException, NoItensException {
        Pedido pedido = this.buscar(codigoPedido);
        ItemPedido itemPedido = this.getItemPedidoByProduto(codigoProduto, pedido.getItens());
        if (itemPedido == null) {
            throw new ItemNotFoundException(MensagemErro.ITEM_NOT_FOUND_IN_LIST.getMessage());
        } else {
            this.atualizaValorTotal(pedido, itemPedido, OperacaoProduto.SUBTRAIR);
            if (itemPedido.getQuantidade() <= 1) {
                return this.removerProduto(codigoPedido, codigoProduto);
            } else {
                itemPedido.reduzirQuantidade();
                return IPedidoRepositoryAdapterGateway.salvar(pedido);
            }
        }
    }

    private void atualizaValorTotal(Pedido pedido, ItemPedido itemPedido, OperacaoProduto operacao)
            throws InvalidOperacaoProdutoException {
        Double valor = pedido.getValorTotal();
        switch (operacao) {
            case REMOVER:
                valor -= (itemPedido.getValorUnitario() * itemPedido.getQuantidade());
                break;
            case SUBTRAIR:
                valor -= itemPedido.getValorUnitario();
                break;
            case ADICIONAR:
                valor += itemPedido.getValorUnitario();
                break;
            default:
                throw new InvalidOperacaoProdutoException(MensagemErro.INVALID_OPERATION.getMessage());
        }
        pedido.atualizarValorTotal(valor);
    }

    private ItemPedido getItemPedidoByProduto(UUID codigoProduto, List<ItemPedido> itemPedidos) {
        return itemPedidos.stream()
                .filter(itemPedido -> itemPedido.getProdutoCodigo().equals(codigoProduto))
                .findFirst()
                .orElse(null);
    }

    private ProdutoPedido getProduto(UUID codigoProduto) throws IntegrationProdutoException {
        try {
            return pedidoProdutoIntegrationAdapterGateway.getProduto(codigoProduto);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IntegrationProdutoException(
                    MensagemErro.PRODUTO_INTEGRATION_ERROR.getMessage() + " " + e.getMessage());
        }
    }

    private boolean isProdutoExisteNoPedido(UUID codigoProduto, Pedido pedido) {
        ItemPedido itemPedido = this.getItemPedidoByProduto(codigoProduto, pedido.getItens());
        return itemPedido != null;
    }
}
