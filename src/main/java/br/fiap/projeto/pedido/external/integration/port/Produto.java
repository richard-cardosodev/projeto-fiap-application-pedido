package br.fiap.projeto.pedido.external.integration.port;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Produto {
    private String codigo;
    private String nome;
    private String descricao;
    private Double preco;
    private String categoria;
    private String imagem;
    private Integer tempoPreparoMin;
}
