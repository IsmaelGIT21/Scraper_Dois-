package br.edu.utfpr.td.tsi.relatorio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import br.edu.utfpr.td.tsi.modelo.Medico;

/**
 * Guarda o que aconteceu com cada médico em cada etapa e gera um CSV no final,
 * para comprovar que todos os registros da fonte foram processados.
 */
public class Relatorio {

    public enum Etapa { HABILITACAO, CADASTRO, REMOCAO }

    public static class Linha {
        private final Medico medico;
        private final Map<Etapa, String> status = new LinkedHashMap<>();
        private String observacao = "";

        Linha(Medico medico) { this.medico = medico; }

        public String status(Etapa etapa) { return status.getOrDefault(etapa, "-"); }
    }

    private final Map<String, Linha> linhas = new LinkedHashMap<>();

    public Relatorio(Collection<Medico> medicos) {
        medicos.forEach(m -> linhas.put(m.getCpf(), new Linha(m)));
    }

    public void registrar(Medico medico, Etapa etapa, String status) {
        registrar(medico, etapa, status, null);
    }

    public void registrar(Medico medico, Etapa etapa, String status, String observacao) {
        Linha linha = linhas.computeIfAbsent(medico.getCpf(), c -> new Linha(medico));
        linha.status.put(etapa, status);
        if (observacao != null && !observacao.isBlank()) {
            linha.observacao = observacao;
        }
    }

    public String status(Medico medico, Etapa etapa) {
        Linha linha = linhas.get(medico.getCpf());
        return linha == null ? "-" : linha.status(etapa);
    }

    /** Quantidade de médicos por status, para uma etapa. */
    public Map<String, Long> resumo(Etapa etapa) {
        Map<String, Long> contagem = new TreeMap<>();
        for (Linha l : linhas.values()) {
            contagem.merge(l.status(etapa), 1L, Long::sum);
        }
        return contagem;
    }

    public void imprimirResumo() {
        System.out.println();
        System.out.println("================ RESUMO ================");
        System.out.println("Médicos na fonte de dados: " + linhas.size());
        for (Etapa etapa : Etapa.values()) {
            Map<String, Long> r = resumo(etapa);
            if (r.size() == 1 && r.containsKey("-")) {
                continue;
            }
            System.out.println(etapa + ": " + r);
        }
        System.out.println("========================================");
    }

    public void salvarCsv(Path arquivo) throws IOException {
        List<String> saida = new ArrayList<>();
        saida.add("﻿nome;cpf;email;crm;especialidades;habilitacao;cadastro;remocao;observacao");
        Function<String, String> esc = s -> s == null ? "" : s.replace(";", ",");
        for (Linha l : linhas.values()) {
            Medico m = l.medico;
            saida.add(String.join(";",
                    esc.apply(m.getNome()), m.getCpf(), m.getEmail(), m.getCrm(),
                    esc.apply(m.getEspecialidades().toString()),
                    l.status(Etapa.HABILITACAO), l.status(Etapa.CADASTRO), l.status(Etapa.REMOCAO),
                    esc.apply(l.observacao)));
        }
        Files.write(arquivo, saida, StandardCharsets.UTF_8);
    }
}
