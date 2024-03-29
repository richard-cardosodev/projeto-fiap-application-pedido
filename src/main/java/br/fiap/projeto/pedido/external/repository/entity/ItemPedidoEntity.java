package br.fiap.projeto.pedido.external.repository.entity;

import br.fiap.projeto.pedido.entity.enums.CategoriaProduto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "item_pedido")
public class ItemPedidoEntity {
    @EmbeddedId
    private ItemPedidoCodigo codigo;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pedidoCodigo")
    @JoinColumn(name = "pedido_codigo")
    private PedidoEntity pedido;
    @Column(nullable = false)
    private int quantidade;
    @Column(nullable = false)
    private String produtoNome;
    @Column(nullable = false)
    private String produtoDescricao;
    @Column(nullable = false, precision = 2)
    private double valorUnitario;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProduto categoriaProduto;
    @Column(nullable = false)
    private String imagem;
    @Column(nullable = false)
    private Integer tempoPreparoMin;
}