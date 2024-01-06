package br.fiap.projeto.pedido.external.integration.port;

import br.fiap.projeto.pedido.entity.enums.StatusComanda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comanda {
    private UUID codigoComanda;
    private UUID codigoPedido;
    private StatusComanda status;
}