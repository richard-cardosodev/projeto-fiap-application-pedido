package br.fiap.projeto.pedido.adapter.mapper;

import br.fiap.projeto.pedido.adapter.controller.rest.response.PedidoDTO;
import br.fiap.projeto.pedido.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public class PedidoDtoMapper {
    public static PedidoDTO toDto(Pedido pedido){
        return new PedidoDTO(pedido.getCodigo(),
                pedido.getItens(),
                pedido.getCliente(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getDataCriacao());
    }
}
