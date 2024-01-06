package br.fiap.projeto.pedido.external.integration.port;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CriaComanda {
    private UUID codigoPedido;
}
