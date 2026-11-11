package br.edu.utfpr.td.tsi.modelo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Estrutura raiz do arquivo: {"status": "sucesso", "dados": [ ... ]}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArquivoMedicos {

    private String status;
    private List<MedicoFonte> dados = new ArrayList<>();

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<MedicoFonte> getDados() { return dados; }
    public void setDados(List<MedicoFonte> dados) { this.dados = dados; }
}
