package br.edu.utfpr.td.tsi.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Registro de um médico exatamente como vem no arquivo JSON de origem.
 * Só os campos usados pelo robô são mapeados; o resto é ignorado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicoFonte {

    @JsonProperty("SG_UF")
    private String uf;

    @JsonProperty("NU_CRM")
    private String crm;

    @JsonProperty("NM_MEDICO")
    private String nome;

    @JsonProperty("ESPECIALIDADE")
    private String especialidade;

    @JsonProperty("SITUACAO")
    private String situacao;

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
}
