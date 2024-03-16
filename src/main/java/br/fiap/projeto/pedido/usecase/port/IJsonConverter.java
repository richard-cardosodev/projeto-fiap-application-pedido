package br.fiap.projeto.pedido.usecase.port;

import br.fiap.projeto.pedido.usecase.exception.JsonProcessingException;

import java.util.Map;

public interface IJsonConverter {
    String convertObjectToJsonString(Object o) throws JsonProcessingException;
    Map<String,Object> stringJsonToMapStringObject(String stringJson) throws JsonProcessingException;
}
