package br.fiap.projeto.pedido.usecase.exception;

public class InvalidOperacaoProdutoException extends Exception{
    public InvalidOperacaoProdutoException(String message) {
        super(message);
    }
}
