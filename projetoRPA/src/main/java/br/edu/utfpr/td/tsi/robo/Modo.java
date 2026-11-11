package br.edu.utfpr.td.tsi.robo;

/** O que o robô deve fazer nesta execução. */
public enum Modo {
    /** Habilita os CPFs (admin), cadastra os médicos e confere a listagem. */
    CADASTRAR,
    /** Cada médico faz login com as próprias credenciais e remove o próprio cadastro. */
    REMOVER,
    /** CADASTRAR seguido de REMOVER (o processo completo pedido no trabalho). */
    COMPLETO
}
