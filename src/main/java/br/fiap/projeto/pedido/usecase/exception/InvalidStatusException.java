package br.fiap.projeto.pedido.usecase.exception;

public class InvalidStatusException extends Exception{
    public InvalidStatusException(String message) {
        super(message);
    }
}