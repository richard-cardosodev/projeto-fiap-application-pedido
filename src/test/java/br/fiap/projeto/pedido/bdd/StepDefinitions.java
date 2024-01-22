package br.fiap.projeto.pedido.bdd;

import br.fiap.projeto.pedido.adapter.controller.port.IPedidoManagementRestAdapterController;
import br.fiap.projeto.pedido.entity.Pedido;
import br.fiap.projeto.pedido.entity.enums.CategoriaProduto;
import br.fiap.projeto.pedido.entity.integration.ProdutoPedido;
import br.fiap.projeto.pedido.usecase.PedidoComandaIntegrationUseCase;
import br.fiap.projeto.pedido.usecase.PedidoManagementUseCase;
import br.fiap.projeto.pedido.usecase.PedidoStatusDataComparator;
import br.fiap.projeto.pedido.usecase.exception.InvalidStatusException;
import br.fiap.projeto.pedido.usecase.exception.NoItensException;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoClienteIntegrationAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoComandaIntegrationAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoProdutoIntegrationAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.adaptergateway.IPedidoRepositoryAdapterGateway;
import br.fiap.projeto.pedido.usecase.port.usecase.IPedidoWorkFlowUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class StepDefinitions {
    private String ENDPOINT_PEDIDO_BASE = "http://localhost:8080/pedido/pedidos/";
    private String ENDPOINT_ADD_PRODUTO = "/adicionar-produto/";
    private String ENDPOINT_ENVIAR_PAGAMENTO = "/pagar";
    private String ENDPOINT_BUSCA_CANCELADOS = "/busca-cancelados";
    private String ENDPOINT_BUSCA_EM_PREPARACAO = "/busca-em-preparacao";
    private String ENDPOINT_BUSCA_ENTREGUES = "/busca-entrgues";
    private String ENDPOINT_BUSCA_PAGOS = "/busca-pagos";
    private String ENDPOINT_BUSCA_PRONTOS = "/busca-prontos";
    private String ENDPOINT_BUSCA_RECEBIDOS = "/busca-recebidos";
    private String ENDPOINT_BUSCA_PEDIDOS = "/busca-pedidos";
    private Map<String,String> dadosPedido;
    private enum genericReturnDataEnum {
        Code,
        Response
    }

    private String CLIENTE_DEFAULT = "681aeab4-9f9c-457c-8d8d-3b5751e41471";
    private String PRODUTO_DEFAULT = "28894d3e-5f18-40da-93c7-49440b91f36b";
    private String PEDIDO_ID = "";
// TESTES EU ESTOU FAZENDO
    @InjectMocks
    private PedidoManagementUseCase pedidoManagementUseCase;
    @InjectMocks
    private PedidoComandaIntegrationUseCase pedidoComandaIntegrationUseCase;
    @Mock
    private IPedidoRepositoryAdapterGateway pedidoRepositoryAdapterGateway;
    @Mock
    private IPedidoProdutoIntegrationAdapterGateway pedidoProdutoIntegrationAdapterGateway;
    @Mock
    private IPedidoClienteIntegrationAdapterGateway pedidoClienteIntegrationAdapterGateway;
    @Mock
    private IPedidoComandaIntegrationAdapterGateway pedidoComandaIntegrationAdapterGateway;
    @Mock
    private IPedidoManagementRestAdapterController pedidoManagementRestAdapterController;
    @Mock
    private IPedidoWorkFlowUseCase pedidoWorkFlowUseCase;
    private Pedido pedido;
    private ProdutoPedido produtoPedido;
    private UUID codigoPedido;
    private UUID codigoProduto;
    private PedidoStatusDataComparator comparator;
    @BeforeEach
    public void setUp() throws InvalidStatusException, NoItensException {
        MockitoAnnotations.initMocks(this);

        pedido = new Pedido(CLIENTE_DEFAULT);
        codigoPedido = UUID.randomUUID();
        codigoProduto = UUID.fromString(PRODUTO_DEFAULT);
        produtoPedido = new ProdutoPedido(codigoProduto, "Produto Teste", "Descrição do Produto", 10.0,
                CategoriaProduto.LANCHE,
                "Imagem", 15);

        comparator = new PedidoStatusDataComparator();

        when(pedidoRepositoryAdapterGateway.salvar(any(Pedido.class))).thenReturn(pedido);
        when(pedidoRepositoryAdapterGateway.buscaPedido(any(UUID.class))).thenReturn(Optional.of(pedido));
        when(pedidoProdutoIntegrationAdapterGateway.getProduto(codigoProduto)).thenReturn(produtoPedido);
        when(pedidoClienteIntegrationAdapterGateway.verificaClienteExiste(UUID.fromString(CLIENTE_DEFAULT))).thenReturn(true);

    }
    @Given("Recebi solicitacao de pedido")
    public void recebi_solicitacao_de_pedido() {
        // Sem código necessário, não há pré-configuracao;
        System.out.println("Recebi a Solicitacao de Pedido");
    }

    @When("Crio o pedido iniciado")
    public void crio_o_pedido_iniciado() {
        dadosPedido = criarPedido("");
    }

    @Then("Visualizo o pedido")
    public void visualizo_o_pedido() {
        System.out.println("Response Code: " + dadosPedido.get(genericReturnDataEnum.Code.name()));
        System.out.println("Response Body: " + dadosPedido.get(genericReturnDataEnum.Response.name()));
        assertEquals("200", dadosPedido.get(genericReturnDataEnum.Code.name()));
    }

    @Given("Recebi solicitacao de pedido com cliente")
    public void recebi_solicitacao_de_pedido_com_cliente() throws InvalidStatusException, NoItensException {
        // Sem código necessário, não há pré-configuracao;
        //setUp();
        System.out.println("Recebi a Solicitacao de Pedido do Cliente: " + CLIENTE_DEFAULT);
    }

    @When("Crio o pedido iniciado com cliente")
    public void crio_o_pedido_iniciado_com_cliente() throws Exception {
        dadosPedido = criarPedido(CLIENTE_DEFAULT);
        //System.out.println(pedidoManagementRestAdapterController.criaPedido(CLIENTE_DEFAULT));
    }

    @Given("Tenho um pedido")
    public void tenho_um_pedido() throws JsonProcessingException {
        dadosPedido = criarPedido(CLIENTE_DEFAULT);
        // ObjectMapper do Jackson para converter a string recuperada de String para Json e de Json para um Mapper
        Map<String, String> resultMap = stringJsonToMapStringString(dadosPedido.get(genericReturnDataEnum.Response.name()));
        PEDIDO_ID = resultMap.get("codigo");
        System.out.println("Tenho o segunte pedido");
        System.out.println("Pedido: "+ PEDIDO_ID);
    }

    @When("Receber um produto")
    public void receber_um_produto() {
        System.out.println("Vou adicionar o seguinte produto");
        System.out.println("Produto: " + PRODUTO_DEFAULT);
    }

    @Then("Adiciono produto ao pedido")
    public void adiciono_produto_ao_pedido() throws JsonProcessingException {
        dadosPedido = adicionarProduto(PEDIDO_ID, PRODUTO_DEFAULT);
        System.out.println("Adicionei o produto ao pedido");
    }

    @Given("Tenho um pedido com itens e iniciado")
    public void tenho_um_pedido_com_itens_e_iniciado() throws JsonProcessingException {
        tenho_um_pedido();
        adiciono_produto_ao_pedido();
        System.out.println(dadosPedido);
    }

    @When("Envio pedido para pagamento")
    public void envio_pedido_para_pagamento() throws JsonProcessingException {
        Map<String, String> resultMap = stringJsonToMapStringString(dadosPedido.get(genericReturnDataEnum.Response.name()));
        PEDIDO_ID = resultMap.get("codigo");
        dadosPedido = enviarPedidoPagamento(PEDIDO_ID);
        System.out.println("Pedido enviado para pagamento");
    }

    @Then("Altera pedido para recebido")
    public void altera_pedido_para_recebido() {
        System.out.println(dadosPedido);
    }

    @Given("Tenho um pedido pago")
    public void tenho_um_pedido_pago() throws JsonProcessingException {
        tenho_um_pedido_com_itens_e_iniciado();
        envio_pedido_para_pagamento();

    }

    @When("Envio para comanda")
    public void envio_para_comanda() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("Altera pedido para em preparacao")
    public void altera_pedido_para_em_preparacao() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("Tenho um pedido pronto")
    public void tenho_um_pedido_pronto() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("Finalizo o pedido")
    public void finalizo_o_pedido() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("Altera pedido para finalizado")
    public void altera_pedido_para_finalizado() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


    ////////////////////////////////////////
    //  FLUXOS ADICIONAIS
    ////////////////////////////////////////
    ////////////////////////////////////////
    //  BUSCAS
    ////////////////////////////////////////
    @Given("Tenho o codigo de um pedido")
    public void tenho_o_codigo_de_um_pedido() throws JsonProcessingException {
        dadosPedido = criarPedido("");
        Map<String, String> resultMap = stringJsonToMapStringString(dadosPedido.get(genericReturnDataEnum.Response.name()));
        PEDIDO_ID = resultMap.get("codigo");
        System.out.println("Tenho o segunte pedido");
        System.out.println("Pedido: "+ PEDIDO_ID);
    }

    @When("Busco o pedido")
    public void busco_o_pedido() {
        dadosPedido = buscarPedido(PEDIDO_ID);
    }

    @Given("Recebo solicitacao para retornar todos os pedidos cancelados")
    public void recebo_solicitacao_para_retornar_todos_os_pedidos_cancelados() {
        System.out.println("Recuperar pedidos cancelados");
    }

    @When("Busco os pedidos cancelado")
    public void busco_os_pedidos_cancelado() {
        dadosPedido = buscarPedidos(ENDPOINT_BUSCA_CANCELADOS);
    }

    @Then("Visualizo os pedidos")
    public void visualizo_os_pedidos() {
        System.out.println("Response Code: " + dadosPedido.get(genericReturnDataEnum.Code.name()));
        System.out.println("Response Body: " + dadosPedido.get(genericReturnDataEnum.Response.name()));
        assertEquals("200", dadosPedido.get(genericReturnDataEnum.Code.name()));
    }

    @Given("Recebo solicitacao para retornar todos os pedidos em preparacao")
    public void recebo_solicitacao_para_retornar_todos_os_pedidos_em_preparacao() {
        System.out.println("Recuperar pedidos em preparacao");
    }

    @When("Busco os pedidos em preparacao")
    public void busco_os_pedidos_em_preparacao() {
        dadosPedido = buscarPedidos(ENDPOINT_BUSCA_EM_PREPARACAO);
    }

    @Given("Recebo solicitacao para retornar todos os pedidos entregues")
    public void recebo_solicitacao_para_retornar_todos_os_pedidos_entregues() {
        System.out.println("Recuperar pedidos entregues");
    }

    @When("Busco os pedidos entregues")
    public void busco_os_pedidos_entregues() {
        dadosPedido = buscarPedidos(ENDPOINT_BUSCA_ENTREGUES);
    }

    @Given("Recebo solicitacao para retornar todos os pedidos pagos")
    public void recebo_solicitacao_para_retornar_todos_os_pedidos_pagos() {
        System.out.println("Recuperar pedidos Pagos");
    }

    @When("Busco os pedidos pagos")
    public void busco_os_pedidos_pagos() {
        dadosPedido = buscarPedidos(ENDPOINT_BUSCA_PAGOS);
    }

    @Given("Recebo solicitacao para retornar todos os pedidos prontos")
    public void recebo_solicitacao_para_retornar_todos_os_pedidos_prontos() {
        System.out.println("Recuperar pedidos prontos");
    }

    @When("Busco os pedidos prontos")
    public void busco_os_pedidos_prontos() {
        dadosPedido = buscarPedidos(ENDPOINT_BUSCA_PRONTOS);
    }

    @Given("Recebo solicitacao para retornar todos os pedidos recebidos")
    public void recebo_solicitacao_para_retornar_todos_os_pedidos_recebidos() {
        System.out.println("Recuperar pedidos recebidos");
    }

    @When("Busco os pedidos recebidos")
    public void busco_os_pedidos_recebidos() {
        dadosPedido = buscarPedidos(ENDPOINT_BUSCA_RECEBIDOS);
    }

    @Given("Recebo solicitacao para retornar todos os pedidos em aberto")
    public void recebo_solicitacao_para_retornar_todos_os_pedidos_em_aberto() {
        System.out.println("Recuperar pedidos em aberto");
    }

    @When("Busco os pedidos em aberto")
    public void busco_os_pedidos_em_aberto() {
        dadosPedido = buscarPedidos(ENDPOINT_BUSCA_PEDIDOS);
    }

    ////////////////////////////////////////
    //  FLUXO DE TRABALHO
    ////////////////////////////////////////
    @When("Recebo solicitacao para cancelar")
    public void recebo_solicitacao_para_cancelar() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("Altero status do pedido para Cancelado")
    public void altero_status_do_pedido_para_cancelado() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    ////////////////////////////////////////
    //  OPERAÇÕES
    ////////////////////////////////////////
    @Given("tenho um pedido com itens")
    public void tenho_um_pedido_com_itens() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("recebo solicitacao para aumentar a quantidade de um produto do pedido")
    public void recebo_solicitacao_para_aumentar_a_quantidade_de_um_produto_do_pedido() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("Aumento em uma unidade a quantidade daquele item no produto")
    public void aumento_em_uma_unidade_a_quantidade_daquele_item_no_produto() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("recebo solicitacao para diminuir a quantidade de um produto do pedido")
    public void recebo_solicitacao_para_diminuir_a_quantidade_de_um_produto_do_pedido() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("Diminuo em uma unidade a quantidade daquele item no produto")
    public void diminuo_em_uma_unidade_a_quantidade_daquele_item_no_produto() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("recebo solicitacao para remover um produto do pedido")
    public void recebo_solicitacao_para_remover_um_produto_do_pedido() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("Removo aquele item no produto")
    public void removo_aquele_item_no_produto() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    // Métodos auxiliares

    private Map<String,String> criarPedido(String codigoCliente){
        String clienteParam = "";
        if(!codigoCliente.isEmpty()){
            clienteParam = "?codigo_cliente=" + codigoCliente;
        }
        return doPostRequest(ENDPOINT_PEDIDO_BASE + clienteParam);
    }
    private Map<String,String> buscarPedido(String codigoPedido){
        return doGetRequest(ENDPOINT_PEDIDO_BASE + codigoPedido);
    }
    private Map<String,String> buscarPedidos(String searchType){
        return doGetRequest(ENDPOINT_PEDIDO_BASE + searchType);
    }
    private Map<String,String> adicionarProduto(String codigoPedido, String codigoProduto){
        return doPostRequest(ENDPOINT_PEDIDO_BASE + codigoPedido + ENDPOINT_ADD_PRODUTO + codigoProduto);
    }

    private Map<String,String> enviarPedidoPagamento(String codigoPedido){
        return doPutRequest(ENDPOINT_PEDIDO_BASE + codigoPedido + ENDPOINT_ENVIAR_PAGAMENTO );
    }

    private Map<String,String> doPostRequest(String endpoint){
        return doRequest(endpoint, "POST");
    }
    private Map<String,String> doGetRequest(String endpoint){
        return doRequest(endpoint, "GET");
    }
    private Map<String,String> doPutRequest(String endpoint){
        return doRequest(endpoint, "PUT");
    }

    private Map<String,String> doRequest(String endpoint, String requestType){
        Map<String,String> retorno = new HashMap<>();
        retorno.put(genericReturnDataEnum.Code.name(),"");
        retorno.put(genericReturnDataEnum.Response.name(),"");
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestType);
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();
            retorno.put(genericReturnDataEnum.Code.name(),String.valueOf(responseCode));

            // Leitura da resposta do servidor
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                retorno.put(genericReturnDataEnum.Response.name(),response.toString());
                return retorno;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retorno;
    }

    private Map<String,String> stringJsonToMapStringString(String stringJson) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        Map<String, String> resultMap = om.readValue(stringJson, Map.class);
        return resultMap;
    }
}
