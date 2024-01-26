package br.fiap.projeto.pedido.util;

import br.fiap.projeto.pedido.external.integration.port.Comanda;
import br.fiap.projeto.pedido.external.integration.port.Pagamento;
import br.fiap.projeto.pedido.external.integration.port.Produto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {


    public static String createProdutoJsonString(Produto produto){
        Map<String,Object> data = new HashMap<>();
        data.put("codigo", produto.getCodigo());
        data.put("nome", produto.getNome());
        data.put("descricao", produto.getDescricao());
        data.put("preco", produto.getPreco());
        data.put("categoria", produto.getCategoria());
        data.put("imagem", produto.getImagem());
        data.put("tempoPreparoMin", produto.getTempoPreparoMin());
        return convertMapToJsonString(data);
    }
    public static String createPagamentoJsonString(Pagamento pagamento){
        Map<String,Object> data = new HashMap<>();
        data.put("codigoPedido", pagamento.getCodigoPedido());
        data.put("dataPagamento", pagamento.getDataPagamento());
        data.put("status", pagamento.getStatus());
        return convertMapToJsonString(data);
    }
    public static String createComandaJsonString(Comanda comanda){
        Map<String,Object> data = new HashMap<>();
        data.put("codigoComanda", comanda.getCodigoComanda());
        data.put("codigoPedido", comanda.getCodigoPedido());
        data.put("status", comanda.getStatus());
        return convertMapToJsonString(data);
    }
    public static String convertMapToJsonString(Map<String, Object> data) {
        try {
            // Create ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the Map to JSON string
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            // Handle exception (e.g., log it or throw a RuntimeException)
            e.printStackTrace();
            return null;
        }
    }
    public static String convertObjectToJsonString(Object o) {
        try {
            // Create ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the Object to JSON string
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            // Handle exception (e.g., log it or throw a RuntimeException)
            e.printStackTrace();
            return null;
        }
    }
    public static Map<String,Object> stringJsonToMapStringObject(String stringJson) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> resultMap = om.readValue(stringJson, Map.class);
        return resultMap;
    }
}
