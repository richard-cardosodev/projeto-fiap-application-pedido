package br.fiap.projeto.pedido;

import br.fiap.projeto.pedido.external.integration.port.Cliente;
import br.fiap.projeto.pedido.external.integration.port.Comanda;
import br.fiap.projeto.pedido.external.integration.port.Pagamento;
import br.fiap.projeto.pedido.external.integration.port.Produto;
import br.fiap.projeto.pedido.external.utils.JsonConverter;
import br.fiap.projeto.pedido.usecase.port.IJsonConverter;
import br.fiap.projeto.pedido.usecase.port.messaging.IPagamentoCanceladoQueueIN;
import br.fiap.projeto.pedido.usecase.port.messaging.IPagamentoConfirmadoQueueIN;
import br.fiap.projeto.pedido.usecase.port.messaging.IPedidoQueueAdapterGatewayOUT;
import br.fiap.projeto.pedido.util.DomainUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;
import java.util.UUID;

import static br.fiap.projeto.pedido.util.Constants.*;
import static br.fiap.projeto.pedido.util.DomainUtils.*;
import static br.fiap.projeto.pedido.util.JsonUtils.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@Slf4j
@AutoConfigureMockMvc
public class PedidoIntegrationTest {

    private static final int PORT = 9080;

    @Autowired
    private MockMvc mvc;

    private static final WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(PORT));

    @BeforeAll
    public static void setUp(){
        log.info("Starting wireMock server");
        wireMockServer.start();
    }

    @AfterAll
    private static void tearDown(){
        log.info("Stopping wireMock server");
        wireMockServer.stop();
    }

    private void wireMockClienteMockUp(){
        String jsonValue = "{\"codigo\": \"" + CLIENTE_DEFAULT + "\"}";
        wireMockServer.stubFor(get(urlEqualTo("/identificacao/clientes/" + CLIENTE_DEFAULT))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private void wireMockProdutoMockUp(){
        Produto lanche = createProdutoLanche();
        String jsonValue = createProdutoJsonString(lanche);
        wireMockServer.stubFor(get(urlEqualTo("/produto/produtos/" + LANCHE_DEFAULT))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private void wireMockPagamentoIniciaMockUp(){
        Pagamento pagamento = createPagamentoPendente();
        String jsonValue = createPagamentoJsonString(pagamento);

        wireMockServer.stubFor(post(urlEqualTo("/pagamento/pagamento" + ENDPOINT_PAGAMENTO_NOVO ))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private void wireMockPagamentoBuscarStatusPorPedidoMockUp(Pagamento pagamento){
        String jsonValue = createPagamentoJsonString(pagamento);
        wireMockServer.stubFor(get(urlEqualTo("/pagamento/pagamento" + ENDPOINT_PAGAMENTO_BUSCA_PEDIDO + CODIGO_PEDIDO))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }
    private void wireMockComandaCreate(){
        Comanda comanda = createComanda();
        String jsonValue = createComandaJsonString(comanda);

        wireMockServer.stubFor(post(urlEqualTo("/comanda/comandas" ))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonValue)));
    }

    @Mock
    private IPedidoQueueAdapterGatewayOUT pedidoQueueAdapterGatewayOUT;

    @Mock
    private IPagamentoConfirmadoQueueIN pagamentoConfirmadoQueueIN;

    @Mock
    private IPagamentoCanceladoQueueIN pagamentoCanceladoQueueIN;

    ////////////////////////////////////////////////////////
    // INICIO DOS TESTES
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
    }
    @Test
    public void testeAdicionarProduto() throws Exception{
        testeCriarComCliente();

        wireMockProdutoMockUp();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ADD_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        Map<String, Object> resultMap = stringJsonToMapStringObject(result.getResponse().getContentAsString());
        DomainUtils.VALOR_PEDIDO = Double.valueOf(resultMap.get("valorTotal").toString());
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void testeRemoverProduto() throws Exception{
        testeAdicionarProduto();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .delete(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_REMOVE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void testeAumentarQuantidadeProduto() throws Exception{
        testeAdicionarProduto();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_INCREASE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void testeRemoverQuantidadeProduto() throws Exception{
        testeAdicionarProduto();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_DECREASE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void testeRemoverQuantidadeProdutoComMaisDeUm() throws Exception{
        testeAumentarQuantidadeProduto();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_DECREASE_PRODUTO + LANCHE_DEFAULT))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void testeEnviarParaPagamento() throws Exception{
        testeAdicionarProduto();
        wireMockPagamentoIniciaMockUp();
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO +ENDPOINT_ENVIAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeRecebeRetornoPagamentoPago() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
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
    }
    @Test
    public void testeRecebeRetornoPagamentoCancelado() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
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
    }
    @Test
    public void testeAtualizaPagamentoPago() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
        Pagamento pagamento = createPagamentoPago();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ATUALIZAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void testeAtualizaPagamentoCancelado() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
        Pagamento pagamento = createPagamentoCancelado();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ATUALIZAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeAtualizaPagamentoFluxoNegativoPedidoNaoExiste() throws Exception{
        // Pre condições
        testeEnviarParaPagamento();

        // Configuracao do wiremock
        Pagamento pagamento = createPagamentoCancelado();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + UUID.randomUUID() + ENDPOINT_ATUALIZAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .is5xxServerError())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeAtualizaPagamentoFluxoNegativoPedidoStatusIncorreto() throws Exception{
        // Pre condições
        testeAtualizaPagamentoPago();

        // Configuracao do wiremock
        Pagamento pagamento = createPagamentoCancelado();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ATUALIZAR_PAGAMENTO))
                .andExpect(MockMvcResultMatchers.status()
                        .is4xxClientError())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeEnviarComanda() throws Exception{
        // Pre condições
        testeAtualizaPagamentoPago();

        // Configuracao do wiremock
        Pagamento pagamento = createPagamentoPago();
        wireMockPagamentoBuscarStatusPorPedidoMockUp(pagamento);
        wireMockComandaCreate();

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .patch(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ENVIAR_COMANDA))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeProntificar() throws Exception{
        // Pre condições
        testeEnviarComanda();

        // Configuracao do wiremock

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_PRONTIFICAR))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeFinalizar() throws Exception{
        // Pre condições
        testeProntificar();

        // Configuracao do wiremock

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT_PEDIDO_BASE + CODIGO_PEDIDO + ENDPOINT_ENTREGAR))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
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
    }
    @Test
    public void testeBuscaProdutosEmPreparacao() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_EM_PREPARACAO);
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeBuscaProdutosEntregues() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_ENTREGUES);
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeBuscaProdutosPagos() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_PAGOS);
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeBuscaProdutosRecebidos() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_RECEBIDOS);
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeBuscaProdutosProntos() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_PRONTOS);
        System.out.println(result.getResponse().getContentAsString());
    }
    @Test
    public void testeBuscaProdutosEmAberto() throws Exception{
        testeCriarComCliente();
        MvcResult result = genericGet(ENDPOINT_PEDIDO_BASE + ENDPOINT_BUSCA_PEDIDOS);
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void testEnviaPedidoParaPagamentoQueue() throws Exception {
        testeAdicionarProduto();
        pedidoQueueAdapterGatewayOUT.publish("Teste mensagem");
        Mockito.verify(pedidoQueueAdapterGatewayOUT, Mockito.times(1)).publish(ArgumentMatchers.anyString());
    }

    @Test
    public void testRecebePagamentoCanceladoQueue() throws Exception {
        testeAdicionarProduto();

        IJsonConverter customConverter = Mockito.mock(JsonConverter.class);
        Mockito.when(customConverter.convertObjectToJsonString(ArgumentMatchers.any(Pagamento.class))).thenCallRealMethod();

        Pagamento pagamento = createPagamentoCancelado();
        String message = customConverter.convertObjectToJsonString(pagamento);
        pagamentoCanceladoQueueIN.receive(message);

        try {
            Mockito.verify(pagamentoCanceladoQueueIN, Mockito.times(1)).receive(ArgumentMatchers.anyString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRecebePagamentoConfirmadoQueue() throws Exception {
        testeAdicionarProduto();

        IJsonConverter customConverter = Mockito.mock(JsonConverter.class);
        Mockito.when(customConverter.convertObjectToJsonString(ArgumentMatchers.any(Pagamento.class))).thenCallRealMethod();

        Pagamento pagamento = createPagamentoPago();
        pagamentoConfirmadoQueueIN.receive(customConverter.convertObjectToJsonString(pagamento));

        try {
            Mockito.verify(pagamentoConfirmadoQueueIN, Mockito.times(1)).receive(ArgumentMatchers.anyString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
