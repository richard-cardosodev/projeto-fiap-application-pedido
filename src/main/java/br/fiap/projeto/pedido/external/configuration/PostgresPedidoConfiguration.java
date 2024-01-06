package br.fiap.projeto.pedido.external.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "br.fiap.projeto.pedido.external.repository")
@EntityScan(basePackages = "br.fiap.projeto.pedido.external.repository.entity")
public class PostgresPedidoConfiguration {
}
