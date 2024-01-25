package br.fiap.projeto.pedido.external.integration.port;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NovoPagamento {
    private String codigoPedido;
    private Double valorTotal;
    //private Date dataPagamento;
    //private List<Pedido> pedidos;
}
