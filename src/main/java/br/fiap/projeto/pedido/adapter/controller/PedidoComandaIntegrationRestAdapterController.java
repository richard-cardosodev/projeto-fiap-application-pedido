package br.fiap.projeto.pedido.adapter.controller;

import br.fiap.projeto.pedido.adapter.controller.port.IPedidoComandaIntegrationRestAdapterController;
import br.fiap.projeto.pedido.adapter.controller.rest.response.PedidoDTO;
import br.fiap.projeto.pedido.adapter.mapper.PedidoDtoMapper;
import br.fiap.projeto.pedido.usecase.port.usecase.IPedidoComandaIntegrationUseCase;

import java.util.UUID;

public class PedidoComandaIntegrationRestAdapterController implements IPedidoComandaIntegrationRestAdapterController {
    private final IPedidoComandaIntegrationUseCase pedidoComandaIntegrationUseCase;

    public PedidoComandaIntegrationRestAdapterController(IPedidoComandaIntegrationUseCase pedidoComandaIntegrationUseCase){
        this.pedidoComandaIntegrationUseCase = pedidoComandaIntegrationUseCase;
    }
    public PedidoDTO criaComanda(UUID codigoPedido) throws Exception {
        return PedidoDtoMapper.toDto(this.pedidoComandaIntegrationUseCase.criaComanda(codigoPedido));
    }
}