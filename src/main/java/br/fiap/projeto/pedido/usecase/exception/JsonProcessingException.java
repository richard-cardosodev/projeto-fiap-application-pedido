package br.fiap.projeto.pedido.usecase.exception;

public class JsonProcessingException extends Exception{
    public JsonProcessingException(String message) {
        super( "Erro ao efetuar a conversão do Json: " + message);
    }
}
