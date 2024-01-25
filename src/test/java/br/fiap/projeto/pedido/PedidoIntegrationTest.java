package br.fiap.projeto.pedido;

import br.fiap.projeto.pedido.entity.enums.CategoriaProduto;
import br.fiap.projeto.pedido.external.integration.port.Cliente;
import br.fiap.projeto.pedido.external.integration.port.Produto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PedidoIntegrationTest {
    @Autowired
    private MockMvc mvc;
    private WireMockServer wireMockServer;
    private String CODIGO_PEDIDO;
    private String CLIENTE_DEFAULT = "681aeab4-9f9c-457c-8d8d-3b5751e41471";
    private String LANCHE_DEFAULT = "28894d3e-5f18-40da-93c7-49440b91f36b";
    private String ENDPOINT_PEDIDO_BASE = "/pedidos/";
    private String ENDPOINT_ADD_PRODUTO = "/adicionar-produto/";
    private String ENDPOINT_REMOVE_PRODUTO = "/remover-produto/";

    private String ENDPOINT_INCREASE_PRODUTO = "/aumentar-qtde-produto/";
    private String ENDPOINT_DECREASE_PRODUTO = "/reduzir-qtde-produto/";
    private String ENDPOINT_ENVIAR_PAGAMENTO = "/pagar";
    private String ENDPOINT_BUSCA_CANCELADOS = "/busca-cancelados";
    private String ENDPOINT_BUSCA_EM_PREPARACAO = "/busca-em-preparacao";
    private String ENDPOINT_BUSCA_ENTREGUES = "/busca-entregues";
    private String ENDPOINT_BUSCA_PAGOS = "/busca-pagos";
    private String ENDPOINT_BUSCA_PRONTOS = "/busca-prontos";
    private String ENDPOINT_BUSCA_RECEBIDOS = "/busca-recebidos";
    private String ENDPOINT_BUSCA_PEDIDOS = "/busca-pedidos";
    private void wireMockServerUp(){
        // Configurar e iniciar o servidor WireMock antes de cada cenário
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.stop();
        wireMockServer.start();
        System.out.println(wireMockServer.baseUrl());
        configureFor("localhost", 8081);
    }
    private void wireMockServerDown(){
        wireMockServer.stop();
    }
    private void wireMockClienteMockUp(){
        String jsonValue = "{\"codigo\": \"" + CLIENTE_DEFAULT + "\"}";
        stubFor(get(urlEqualTo("/identificacao/clientes/" + CLIENTE_DEFAULT))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private void wireMockProdutoMockUp(){
        Produto lanche = new Produto(LANCHE_DEFAULT
                ,"X-TUDO"
                ,"DA LANCHONETE PRA UTI"
                ,20d
                ,CategoriaProduto.LANCHE.toString()
                ,"link de uma imagem bem legal"
                ,10);
        String jsonValue = createProdutoJsonString(lanche);
        stubFor(get(urlEqualTo("/produto/produtos/" + LANCHE_DEFAULT))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private String createProdutoJsonString(Produto produto){
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
    private static String convertMapToJsonString(Map<String, Object> data) {
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
    private Map<String,String> stringJsonToMapStringString(String stringJson) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        Map<String, String> resultMap = om.readValue(stringJson, Map.class);
        return resultMap;
    }
    @Test
    public void testeCriar() throws Exception{
        mvc.perform(MockMvcRequestBuilders
                        .post("/pedidos"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
    }

    @Test
    public void testeCriarComCliente() throws Exception{
        wireMockServerUp();
        wireMockClienteMockUp();
        Cliente cliente_teste = new Cliente(CLIENTE_DEFAULT);
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post("/pedidos").queryParam("codigo_cliente", CLIENTE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        Map<String, String> resultMap = stringJsonToMapStringString(result.getResponse().getContentAsString());
        CODIGO_PEDIDO = resultMap.get("codigo");
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeAdicionarProduto() throws Exception{
        testeCriarComCliente();

        wireMockServerUp();
        wireMockProdutoMockUp();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ADD_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeRemoverProduto() throws Exception{
        testeAdicionarProduto();

        wireMockServerUp();
        wireMockProdutoMockUp();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .delete(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_REMOVE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }

    @Test
    public void testeAumentarQuantidadeProduto() throws Exception{
        testeAdicionarProduto();

        wireMockServerUp();
        wireMockProdutoMockUp();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_INCREASE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }

    @Test
    public void testeRemoverQuantidadeProduto() throws Exception{
        testeAdicionarProduto();

        wireMockServerUp();
        //wireMockProdutoMockUp();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_DECREASE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }

    ////////////////////////////////////////
    //  FLUXOS ADICIONAIS
    ////////////////////////////////////////
    ////////////////////////////////////////
    //  BUSCAS
    ////////////////////////////////////////
    private MvcResult genericGet(String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                        .get(url ))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
    }
    @Test
    public void testeBuscaProduto() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO);
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeBuscaProdutosCancelados() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_CANCELADOS);
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeBuscaProdutosEmPreparacao() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_EM_PREPARACAO);
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeBuscaProdutosEntregues() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_ENTREGUES);
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeBuscaProdutosPagos() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_PAGOS);
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeBuscaProdutosRecebidos() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_RECEBIDOS);
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeBuscaProdutosProntos() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_PRONTOS);
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeBuscaProdutosEmAberto() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_PEDIDOS);
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
}
