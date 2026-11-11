package br.edu.utfpr.td.tsi.modelo;

import java.util.EnumSet;
import java.util.Set;

/** Médico já convertido para os dados que o formulário da aplicação espera. */
public class Medico {

    private final String nome;
    private final String email;
    private final String cpf;
    private final String crm;
    private final String senha;
    private final Set<Especialidade> especialidades;

    public Medico(String nome, String email, String cpf, String crm, String senha, Set<Especialidade> especialidades) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.crm = crm;
        this.senha = senha;
        this.especialidades = especialidades.isEmpty()
                ? EnumSet.noneOf(Especialidade.class)
                : EnumSet.copyOf(especialidades);
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public String getCrm() { return crm; }
    public String getSenha() { return senha; }
    public Set<Especialidade> getEspecialidades() { return especialidades; }

    @Override
    public String toString() {
        return nome + " [" + cpf + "]";
    }
}
