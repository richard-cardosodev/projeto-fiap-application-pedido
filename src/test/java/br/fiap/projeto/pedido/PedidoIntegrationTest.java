package br.fiap.projeto.pedido;

import br.fiap.projeto.pedido.entity.enums.CategoriaProduto;
import br.fiap.projeto.pedido.entity.enums.StatusComanda;
import br.fiap.projeto.pedido.entity.enums.StatusPagamento;
import br.fiap.projeto.pedido.external.integration.port.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PedidoIntegrationTest {

    @Value("${externalport}")
    private int port;

    @Autowired
    private MockMvc mvc;
    private WireMockServer wireMockServer;
    private String CODIGO_PEDIDO;
    private Double VALOR_PEDIDO = 0d;
    private String CLIENTE_DEFAULT = "681aeab4-9f9c-457c-8d8d-3b5751e41471";
    private String LANCHE_DEFAULT = "28894d3e-5f18-40da-93c7-49440b91f36b";
    private String COMANDA_DEFAULT = "3eaf22f2-1812-4c1e-af03-f77b66d8399d";
    private String ENDPOINT_PEDIDO_BASE = "/pedidos/";
    private String ENDPOINT_ADD_PRODUTO = "/adicionar-produto/";
    private String ENDPOINT_REMOVE_PRODUTO = "/remover-produto/";
    private String ENDPOINT_INCREASE_PRODUTO = "/aumentar-qtde-produto/";
    private String ENDPOINT_DECREASE_PRODUTO = "/reduzir-qtde-produto/";
    private String ENDPOINT_ENVIAR_PAGAMENTO = "/pagar";
    private String ENDPOINT_RECEBE_RETORNO_PAGAMENTO = "/recebe-retorno-pagamento";
    private String ENDPOINT_ATUALIZAR_PAGAMENTO = "/atualizar-pagamento";
    private String ENDPOINT_PRONTIFICAR = "/prontificar";
    private String ENDPOINT_ENTREGAR = "/entregar";
    private String ENDPOINT_ENVIAR_COMANDA = "/enviar-comanda";
    private String ENDPOINT_CANCELAR = "/cancelar";
    private String ENDPOINT_BUSCA_CANCELADOS = "/busca-cancelados";
    private String ENDPOINT_BUSCA_EM_PREPARACAO = "/busca-em-preparacao";
    private String ENDPOINT_BUSCA_ENTREGUES = "/busca-entregues";
    private String ENDPOINT_BUSCA_PAGOS = "/busca-pagos";
    private String ENDPOINT_BUSCA_PRONTOS = "/busca-prontos";
    private String ENDPOINT_BUSCA_RECEBIDOS = "/busca-recebidos";
    private String ENDPOINT_BUSCA_PEDIDOS = "/busca-pedidos";
    private String ENDPOINT_PAGAMENTO_BUSCA_PEDIDO = "/busca/por-codigo-pedido/";
    private String ENDPOINT_PAGAMENTO_NOVO = "/processa/novo-pagamento";

    //////////////////////////////////////////////////////////////////////////////
    //  wiremock
    //////////////////////////////////////////////////////////////////////////////
    private void wireMockServerUp(){
        // Configurar e iniciar o servidor WireMock antes de cada cenário
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
        wireMockServer.stop();
        wireMockServer.start();
        System.out.println(wireMockServer.baseUrl());
        configureFor("localhost", port);
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
        Produto lanche = createProdutoLanche();
        String jsonValue = createProdutoJsonString(lanche);
        stubFor(get(urlEqualTo("/produto/produtos/" + LANCHE_DEFAULT))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private Produto createProdutoLanche(){
        return new Produto(LANCHE_DEFAULT
                ,"X-TUDO"
                ,"DA LANCHONETE PRA UTI"
                ,20d
                ,CategoriaProduto.LANCHE.toString()
                ,"link de uma imagem bem legal"
                ,10);
    }
    private void wireMockPagamentoIniciaMockUp(){
        Pagamento pagamento = createPagamentoPendente();
        String jsonValue = createPagamentoJsonString(pagamento);

        stubFor(post(urlEqualTo("/pagamento/pagamento" + ENDPOINT_PAGAMENTO_NOVO ))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private void wireMockPagamentoBuscarStatusPorPedidoMockUp(Pagamento pagamento){
        String jsonValue = createPagamentoJsonString(pagamento);
        stubFor(get(urlEqualTo("/pagamento/pagamento" + ENDPOINT_PAGAMENTO_BUSCA_PEDIDO + CODIGO_PEDIDO))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private void wireMockComandaCreate(){
        Comanda comanda = createComanda();
        String jsonValue = createComandaJsonString(comanda);

        stubFor(post(urlEqualTo("/comanda/comandas" ))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    /////////////////////////////////////////////////
    // AUXILIARES DO WIREMOCK
    /////////////////////////////////////////////////
    private NovoPagamento createNovoPagamento(){
        return new NovoPagamento(CODIGO_PEDIDO,VALOR_PEDIDO);
    }
    private Pagamento createPagamentoPendente(){
        return new Pagamento(CODIGO_PEDIDO,getCurrentDate(), StatusPagamento.PENDING);
    }
    private Pagamento createPagamentoPago(){
        return new Pagamento(CODIGO_PEDIDO,getCurrentDate(), StatusPagamento.APPROVED);
    }
    private Pagamento createPagamentoCancelado(){
        return new Pagamento(CODIGO_PEDIDO,getCurrentDate(), StatusPagamento.CANCELLED);
    }
    private Comanda createComanda(){
        return new Comanda(UUID.fromString(COMANDA_DEFAULT),UUID.fromString(CODIGO_PEDIDO), StatusComanda.RECEBIDO);
    }
    private Date getCurrentDate(){
        // Obter a data e hora atual
        LocalDateTime currentDateTime = LocalDateTime.now();
        // Converter LocalDateTime para Instant
        Instant instant = currentDateTime.atZone(ZoneId.systemDefault()).toInstant();
        // Criar um objeto Date a partir do Instant
        return Date.from(instant);
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
    private String createPagamentoJsonString(Pagamento pagamento){
        Map<String,Object> data = new HashMap<>();
        data.put("codigoPedido", pagamento.getCodigoPedido());
        data.put("dataPagamento", pagamento.getDataPagamento());
        data.put("status", pagamento.getStatus());
        return convertMapToJsonString(data);
    }
    private String createComandaJsonString(Comanda comanda){
        Map<String,Object> data = new HashMap<>();
        data.put("codigoComanda", comanda.getCodigoComanda());
        data.put("codigoPedido", comanda.getCodigoPedido());
        data.put("status", comanda.getStatus());
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
    private static String convertObjectToJsonString(Object o) {
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
    private Map<String,Object> stringJsonToMapStringObject(String stringJson) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> resultMap = om.readValue(stringJson, Map.class);
        return resultMap;
    }
    ////////////////////////////////////////////////////////
    // INICIO DOS TESTE
    ///////////////////////////////////////////////////////

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
        Map<String, Object> resultMap = stringJsonToMapStringObject(result.getResponse().getContentAsString());
        CODIGO_PEDIDO = resultMap.get("codigo").toString();
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
        Map<String, Object> resultMap = stringJsonToMapStringObject(result.getResponse().getContentAsString());
        VALOR_PEDIDO = Double.valueOf(resultMap.get("valorTotal").toString());
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeRemoverProduto() throws Exception{
        testeAdicionarProduto();

        wireMockServerUp();
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
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_DECREASE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }

    @Test
    public void testeRemoverQuantidadeProdutoComMaisDeUm() throws Exception{
        testeAumentarQuantidadeProduto();

        wireMockServerUp();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_DECREASE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }

    @Test
    public void testeEnviarParaPagamento() throws Exception{
        testeAdicionarProduto();
        wireMockServerUp();
        wireMockPagamentoIniciaMockUp();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO +ENDPOINT_ENVIAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeRecebeRetornoPagamentoPago() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
        wireMockServerUp();
        Pagamento pagamento = createPagamentoPago();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();
        // Montagem da requisição
        String jsonEnviado = convertObjectToJsonString(pagamento);

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT_PEDIDO_BASE + ENDPOINT_RECEBE_RETORNO_PAGAMENTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEnviado))
                .andExpect(MockMvcResultMatchers.status()
                        .isNoContent())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeRecebeRetornoPagamentoCancelado() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
        wireMockServerUp();
        Pagamento pagamento = createPagamentoCancelado();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();
        // Montagem da requisição
        String jsonEnviado = convertObjectToJsonString(pagamento);

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT_PEDIDO_BASE + ENDPOINT_RECEBE_RETORNO_PAGAMENTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEnviado))
                .andExpect(MockMvcResultMatchers.status()
                        .isNoContent())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeAtualizaPagamentoPago() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
        wireMockServerUp();
        Pagamento pagamento = createPagamentoPago();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ATUALIZAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }

    @Test
    public void testeAtualizaPagamentoCancelado() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
        wireMockServerUp();
        Pagamento pagamento = createPagamentoCancelado();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ATUALIZAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeAtualizaPagamentoFluxoNegativoPedidoNaoExiste() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
        wireMockServerUp();
        Pagamento pagamento = createPagamentoCancelado();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + UUID.randomUUID() + ENDPOINT_ATUALIZAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .is5xxServerError())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeAtualizaPagamentoFluxoNegativoPedidoStatusIncorreto() throws Exception{
        // Pre condições
        testeAtualizaPagamentoPago();

        // Configuracao do wiremock
        wireMockServerUp();
        Pagamento pagamento = createPagamentoCancelado();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ATUALIZAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .is4xxClientError())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeEnviarComanda() throws Exception{
        // Pre condições
        testeAtualizaPagamentoPago();

        // Configuracao do wiremock
        wireMockServerUp();
        Pagamento pagamento = createPagamentoPago();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ENVIAR_COMANDA))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeProntificar() throws Exception{
        // Pre condições
        testeEnviarComanda();

        // Configuracao do wiremock
        wireMockServerUp();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_PRONTIFICAR))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        wireMockServerDown();
    }
    @Test
    public void testeFinalizar() throws Exception{
        // Pre condições
        testeProntificar();

        // Configuracao do wiremock
        wireMockServerUp();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ENTREGAR))
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
