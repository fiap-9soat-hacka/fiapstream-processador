package com.fiap.enums;

public enum EstadoProcessamento {
    PENDENTE("Pendente"),
    PROCESSANDO("Processando"),
    CONCLUIDO("Concluído"),
    ERRO("Erro");

    private final String descricao;

    EstadoProcessamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
