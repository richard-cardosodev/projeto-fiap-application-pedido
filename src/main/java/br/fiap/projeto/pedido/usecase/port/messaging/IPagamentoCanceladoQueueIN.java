package br.fiap.projeto.pedido.usecase.port.messaging;

public interface IPagamentoCanceladoQueueIN {
    void receive(String message);
}
