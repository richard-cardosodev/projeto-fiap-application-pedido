package br.fiap.projeto.pedido.util;

import br.fiap.projeto.pedido.entity.enums.CategoriaProduto;
import br.fiap.projeto.pedido.entity.enums.StatusComanda;
import br.fiap.projeto.pedido.entity.enums.StatusPagamento;
import br.fiap.projeto.pedido.external.integration.port.Comanda;
import br.fiap.projeto.pedido.external.integration.port.NovoPagamento;
import br.fiap.projeto.pedido.external.integration.port.Pagamento;
import br.fiap.projeto.pedido.external.integration.port.Produto;

import java.util.UUID;

import static br.fiap.projeto.pedido.util.Constants.COMANDA_DEFAULT;
import static br.fiap.projeto.pedido.util.Constants.LANCHE_DEFAULT;
import static br.fiap.projeto.pedido.util.DateUtils.getCurrentDate;

public class DomainUtils {

    public static String CODIGO_PEDIDO;

    public static Double VALOR_PEDIDO = 0d;

    public NovoPagamento createNovoPagamento(){
        return new NovoPagamento(CODIGO_PEDIDO,VALOR_PEDIDO);
    }
    public static Pagamento createPagamentoPendente(){
        return new Pagamento(CODIGO_PEDIDO, getCurrentDate(), StatusPagamento.PENDING);
    }
    public static Pagamento createPagamentoPago(){
        return new Pagamento(CODIGO_PEDIDO, getCurrentDate(), StatusPagamento.APPROVED);
    }
    public static Pagamento createPagamentoCancelado(){
        return new Pagamento(CODIGO_PEDIDO, getCurrentDate(), StatusPagamento.CANCELLED);
    }
    public static Comanda createComanda(){
        return new Comanda(UUID.fromString(COMANDA_DEFAULT),UUID.fromString(CODIGO_PEDIDO), StatusComanda.RECEBIDO);
    }

    public static Produto createProdutoLanche(){
        return new Produto(LANCHE_DEFAULT
                ,"X-TUDO"
                ,"DA LANCHONETE PRA UTI"
                ,20d
                , CategoriaProduto.LANCHE.toString()
                ,"link de uma imagem bem legal"
                ,10);
    }
}
