Feature: Pedido

  # Fluxo principal de trabalho

  Scenario: Criar pedido sem cliente
    Given Recebi solicitacao de pedido
    When Crio o pedido iniciado
    Then Visualizo o pedido

  Scenario: Criar pedido com cliente
    Given Recebi solicitacao de pedido com cliente
    When Crio o pedido iniciado com cliente
    Then Visualizo o pedido

  Scenario: Adicionar item ao pedido
    Given Tenho um pedido
    When Receber um produto
    Then Adiciono produto ao pedido
    Then Visualizo o pedido

  Scenario: Enviar pedido para pagamento
    Given Tenho um pedido com itens e iniciado
    When Envio pedido para pagamento
    Then Altera pedido para recebido

  Scenario: Enviar pedido para comanda
    Given Tenho um pedido pago
    When Envio para comanda
    Then Altera pedido para em preparacao

  Scenario: Finalizar pedido
    Given Tenho um pedido pronto
    When Finalizo o pedido
    Then Altera pedido para finalizado

  # Demais Fluxos

  # Buscas
  Scenario: Buscar Pedido pelo codigo
    Given Tenho o codigo de um pedido
    When Busco o pedido
    Then Visualizo o pedido

  Scenario: Buscar Pedidos Cancelados
    Given Recebo solicitacao para retornar todos os pedidos cancelados
    When Busco os pedidos cancelado
    Then Visualizo os pedidos

  Scenario: Buscar Pedidos em preparacao
    Given Recebo solicitacao para retornar todos os pedidos em preparacao
    When Busco os pedidos em preparacao
    Then Visualizo os pedidos

  Scenario: Buscar Pedidos Entregues
    Given Recebo solicitacao para retornar todos os pedidos entregues
    When Busco os pedidos entregues
    Then Visualizo os pedidos

  Scenario: Buscar Pedidos Pagos
    Given Recebo solicitacao para retornar todos os pedidos pagos
    When Busco os pedidos pagos
    Then Visualizo os pedidos

  Scenario: Buscar Pedidos Prontos
    Given Recebo solicitacao para retornar todos os pedidos prontos
    When Busco os pedidos prontos
    Then Visualizo os pedidos

  Scenario: Buscar Pedidos Recebidos
    Given Recebo solicitacao para retornar todos os pedidos recebidos
    When Busco os pedidos recebidos
    Then Visualizo os pedidos

  Scenario: Buscar Pedidos em Aberto
    Given Recebo solicitacao para retornar todos os pedidos em aberto
    When Busco os pedidos em aberto
    Then Visualizo os pedidos

  # Fluxo de trabalho
  Scenario: Cancelar Pedido
    Given Tenho um pedido
    When Recebo solicitacao para cancelar
    Then Altero status do pedido para Cancelado

  #Operacoes
  Scenario: Aumentar quantidade do Produto
    Given tenho um pedido com itens
    When recebo solicitacao para aumentar a quantidade de um produto do pedido
    Then Aumento em uma unidade a quantidade daquele item no produto

  Scenario: Diminuir quantidade do Produto
    Given tenho um pedido com itens
    When recebo solicitacao para diminuir a quantidade de um produto do pedido
    Then Diminuo em uma unidade a quantidade daquele item no produto

  Scenario: Remover Produto do Pedido
    Given tenho um pedido com itens
    When recebo solicitacao para remover um produto do pedido
    Then Removo aquele item no produto