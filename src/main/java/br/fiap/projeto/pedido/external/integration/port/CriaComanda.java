package br.fiap.projeto.pedido.external.integration.port;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CriaComanda {
    private UUID codigoPedido;
}
