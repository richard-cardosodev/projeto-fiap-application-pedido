package br.fiap.projeto.pedido.adapter.controller;

import br.fiap.projeto.pedido.adapter.controller.port.IPedidoManagementRestAdapterController;
import br.fiap.projeto.pedido.adapter.controller.rest.response.PedidoDTO;
import br.fiap.projeto.pedido.adapter.mapper.PedidoDtoMapper;
import br.fiap.projeto.pedido.usecase.exception.InvalidStatusException;
import br.fiap.projeto.pedido.usecase.exception.NoItensException;
import br.fiap.projeto.pedido.usecase.port.usecase.IPedidoManagementUseCase;

import java.util.UUID;

public class PedidoManagementRestAdapterController implements IPedidoManagementRestAdapterController {
    private final IPedidoManagementUseCase pedidoManagementUseCase;

    public PedidoManagementRestAdapterController(IPedidoManagementUseCase pedidoManagementUseCase) {
        this.pedidoManagementUseCase = pedidoManagementUseCase;
    }

    @Override
    public PedidoDTO criaPedido(String codigoCliente) throws InvalidStatusException, NoItensException {
        return PedidoDtoMapper.toDto(this.pedidoManagementUseCase.criaPedido(codigoCliente));
    }

    @Override
    public PedidoDTO adicionarProduto(UUID codigoPedido, UUID codigoProduto) throws Exception {
        return PedidoDtoMapper.toDto(this.pedidoManagementUseCase.adicionarProduto(codigoPedido, codigoProduto));
    }

    @Override
    public void removerProduto(UUID codigoPedido, UUID produtoCodigo) throws Exception {
        this.pedidoManagementUseCase.removerProduto(codigoPedido, produtoCodigo);
    }

    @Override
    public PedidoDTO aumentarQuantidade(UUID codigoPedido, UUID produtoCodigo) throws Exception {
        return PedidoDtoMapper.toDto(this.pedidoManagementUseCase.aumentarQuantidade(codigoPedido, produtoCodigo));
    }

    @Override
    public PedidoDTO reduzirQuantidade(UUID codigoPedido, UUID produtoCodigo) throws Exception {
        return PedidoDtoMapper.toDto(this.pedidoManagementUseCase.reduzirQuantidade(codigoPedido, produtoCodigo));
    }
}
