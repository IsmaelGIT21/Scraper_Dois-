package br.edu.utfpr.td.tsi.conversao;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import br.edu.utfpr.td.tsi.modelo.Especialidade;
import br.edu.utfpr.td.tsi.modelo.Medico;
import br.edu.utfpr.td.tsi.modelo.MedicoFonte;

/**
 * Converte o registro do JSON nos dados do cadastro, seguindo os padrões do enunciado:
 * <ul>
 *   <li>CPF: CRM-CRM-UF</li>
 *   <li>CRM: CRM-UF</li>
 *   <li>E-mail: PRIMEIRO_NOME-CRM-UF@medicos.com</li>
 *   <li>Senha inicial: CRM-UF</li>
 * </ul>
 */
public class ConversorMedico {

    public Medico converter(MedicoFonte fonte) {
        String nome = fonte.getNome().trim().replaceAll("\\s+", " ");
        String crm = fonte.getCrm().trim();
        String uf = fonte.getUf().trim().toUpperCase(Locale.ROOT);

        String crmUf = crm + "-" + uf;
        String cpf = crm + "-" + crmUf;
        String email = primeiroNome(nome) + "-" + crmUf + "@medicos.com";

        return new Medico(nome, email, cpf, crmUf, crmUf, especialidades(fonte.getEspecialidade()));
    }

    /** Primeiro nome sem acentos/símbolos (o campo é type="email" e o navegador rejeita acentos). */
    static String primeiroNome(String nome) {
        String primeiro = nome.split(" ")[0];
        String semAcento = removerAcentos(primeiro).replaceAll("[^A-Za-z0-9]", "");
        return semAcento.isEmpty() ? "MEDICO" : semAcento.toUpperCase(Locale.ROOT);
    }

    /**
     * O campo ESPECIALIDADE vem no formato "&PEDIATRIA - RQE N.: 123 (Áreas de atuação: ...)&CARDIOLOGIA - RQE ...".
     * Cada trecho entre '&' é uma especialidade; só a parte antes de " - RQE" / "(" interessa.
     * Especialidades que o sistema não oferece são ignoradas.
     */
    static Set<Especialidade> especialidades(String campo) {
        Set<Especialidade> resultado = EnumSet.noneOf(Especialidade.class);
        if (campo == null || campo.isBlank()) {
            return resultado;
        }
        for (String trecho : campo.split("&")) {
            String nomeEsp = removerAcentos(trecho).toUpperCase(Locale.ROOT);
            nomeEsp = nomeEsp.split(" - RQE|\\(|/")[0].trim();
            for (Especialidade e : Especialidade.values()) {
                if (nomeEsp.startsWith(e.getPrefixoNaFonte())) {
                    resultado.add(e);
                }
            }
        }
        return resultado;
    }

    static String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
