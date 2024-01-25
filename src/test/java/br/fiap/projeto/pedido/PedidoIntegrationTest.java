package br.fiap.projeto.pedido;

import br.fiap.projeto.pedido.external.integration.port.Cliente;
import br.fiap.projeto.pedido.external.integration.port.Comanda;
import br.fiap.projeto.pedido.external.integration.port.Pagamento;
import br.fiap.projeto.pedido.external.integration.port.Produto;
import br.fiap.projeto.pedido.util.DomainUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Map;
import java.util.UUID;

import static br.fiap.projeto.pedido.util.Constants.CLIENTE_DEFAULT;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_ADD_PRODUTO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_ATUALIZAR_PAGAMENTO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_BUSCA_CANCELADOS;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_BUSCA_EM_PREPARACAO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_BUSCA_ENTREGUES;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_BUSCA_PAGOS;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_BUSCA_PEDIDOS;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_BUSCA_PRONTOS;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_BUSCA_RECEBIDOS;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_DECREASE_PRODUTO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_ENTREGAR;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_ENVIAR_COMANDA;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_ENVIAR_PAGAMENTO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_INCREASE_PRODUTO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_PAGAMENTO_BUSCA_PEDIDO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_PAGAMENTO_NOVO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_PEDIDO_BASE;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_PRONTIFICAR;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_RECEBE_RETORNO_PAGAMENTO;
import static br.fiap.projeto.pedido.util.Constants.ENDPOINT_REMOVE_PRODUTO;
import static br.fiap.projeto.pedido.util.Constants.LANCHE_DEFAULT;
import static br.fiap.projeto.pedido.util.DomainUtils.CODIGO_PEDIDO;
import static br.fiap.projeto.pedido.util.DomainUtils.createComanda;
import static br.fiap.projeto.pedido.util.DomainUtils.createPagamentoCancelado;
import static br.fiap.projeto.pedido.util.DomainUtils.createPagamentoPago;
import static br.fiap.projeto.pedido.util.DomainUtils.createPagamentoPendente;
import static br.fiap.projeto.pedido.util.DomainUtils.createProdutoLanche;
import static br.fiap.projeto.pedido.util.JsonUtils.convertObjectToJsonString;
import static br.fiap.projeto.pedido.util.JsonUtils.createComandaJsonString;
import static br.fiap.projeto.pedido.util.JsonUtils.createPagamentoJsonString;
import static br.fiap.projeto.pedido.util.JsonUtils.createProdutoJsonString;
import static br.fiap.projeto.pedido.util.JsonUtils.stringJsonToMapStringObject;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@SpringBootTest
@AutoConfigureMockMvc
public class PedidoIntegrationTest {

    @Value("${externalport}")
    private int port;

    @Autowired
    private MockMvc mvc;
    private final WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));;

    @BeforeEach // Configurar e iniciar o servidor WireMock antes de cada cenário
    public void wireMockServerUp(){
        wireMockServer.start();
    }

    @AfterEach // Derrubar o servidor WireMock após cada cenário
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
        wireMockServerDown();
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
}
