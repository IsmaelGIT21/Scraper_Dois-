package br.edu.utfpr.td.tsi.modelo;

/**
 * Especialidades aceitas pela aplicação Clínica Médica.
 * O nome da constante é o mesmo valor que o checkbox do formulário envia.
 */
public enum Especialidade {
    CLINICA_MEDICA("CLINICA MEDICA"),
    PEDIATRIA("PEDIATRIA"),
    GINECOLOGIA("GINECOLOGIA"),
    CIRURGIA_GERAL("CIRURGIA GERAL"),
    CARDIOLOGIA("CARDIOLOGIA"),
    ORTOPEDIA("ORTOPEDIA"),
    NEUROLOGIA("NEUROLOGIA"),
    PSIQUIATRIA("PSIQUIATRIA"),
    DERMATOLOGIA("DERMATOLOGIA"),
    OFTALMOLOGIA("OFTALMOLOGIA");

    /** Prefixo (sem acentos, em maiúsculas) como aparece no campo ESPECIALIDADE do JSON. */
    private final String prefixoNaFonte;

    Especialidade(String prefixoNaFonte) {
        this.prefixoNaFonte = prefixoNaFonte;
    }

    public String getPrefixoNaFonte() {
        return prefixoNaFonte;
    }
}
