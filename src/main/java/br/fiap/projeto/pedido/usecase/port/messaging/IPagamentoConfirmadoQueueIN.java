package br.fiap.projeto.pedido.usecase.port.messaging;

public interface IPagamentoConfirmadoQueueIN {
    void receive(String message);
}
