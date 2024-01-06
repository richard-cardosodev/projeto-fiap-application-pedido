package br.fiap.projeto.pedido.entity.enums;

public enum StatusPagamento {

	APPROVED("Aprovado"),
	CANCELLED("Cancelado"),
	IN_PROCESS("Em processamento"),
	PENDING("Pendente"),
	REJECTED("Rejeitado")

	;

	private final String descricao;

	StatusPagamento(String descricao){
		this.descricao = descricao;
	}

	public String getDescricao(){
		return descricao;
	}
}
 
