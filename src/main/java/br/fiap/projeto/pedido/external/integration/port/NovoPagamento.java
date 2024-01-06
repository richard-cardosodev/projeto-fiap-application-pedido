package br.fiap.projeto.pedido.external.integration.port;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NovoPagamento {
    private String codigoPedido;
    private Double valorTotal;
    //private Date dataPagamento;
    //private List<Pedido> pedidos;
}
