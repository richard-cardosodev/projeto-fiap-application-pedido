package br.fiap.projeto.pedido.external.integration.port;

import br.fiap.projeto.pedido.entity.enums.StatusComanda;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Comanda {
    private UUID codigoComanda;
    private UUID codigoPedido;
    private StatusComanda status;
}