package br.fiap.projeto.pedido.external.integration.port;

import br.fiap.projeto.pedido.entity.enums.StatusPagamento;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pagamento {
    private String codigoPedido;
    private Date dataPagamento;
    private StatusPagamento status;
}
