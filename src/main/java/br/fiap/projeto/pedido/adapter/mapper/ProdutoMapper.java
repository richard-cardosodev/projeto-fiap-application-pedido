package br.fiap.projeto.pedido.adapter.mapper;

import br.fiap.projeto.pedido.entity.enums.CategoriaProduto;
import br.fiap.projeto.pedido.entity.integration.ProdutoPedido;
import br.fiap.projeto.pedido.external.integration.port.Produto;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProdutoMapper {
    public static ProdutoPedido toProdutoPedido(Produto produto){
        return new ProdutoPedido(UUID.fromString(produto.getCodigo()),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                CategoriaProduto.valueOf(produto.getCategoria()),
                produto.getImagem(),
                produto.getTempoPreparoMin());
    }
}
