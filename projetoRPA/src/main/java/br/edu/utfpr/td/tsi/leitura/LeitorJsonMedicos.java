package br.edu.utfpr.td.tsi.leitura;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.utfpr.td.tsi.modelo.ArquivoMedicos;
import br.edu.utfpr.td.tsi.modelo.MedicoFonte;

/** Lê um arquivo JSON de médicos, ou todos os *.json de uma pasta. */
public class LeitorJsonMedicos {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<MedicoFonte> ler(Path origem) throws IOException {
        List<MedicoFonte> medicos = new ArrayList<>();
        for (Path arquivo : listarArquivos(origem)) {
            ArquivoMedicos conteudo = mapper.readValue(arquivo.toFile(), ArquivoMedicos.class);
            List<MedicoFonte> dados = conteudo.getDados() == null ? List.of() : conteudo.getDados();
            System.out.printf("  %-20s %4d médicos%n", arquivo.getFileName(), dados.size());
            medicos.addAll(dados);
        }
        return medicos;
    }

    private List<Path> listarArquivos(Path origem) throws IOException {
        if (Files.isRegularFile(origem)) {
            return List.of(origem);
        }
        if (!Files.isDirectory(origem)) {
            throw new IOException("Origem de dados não encontrada: " + origem.toAbsolutePath());
        }
        try (Stream<Path> arquivos = Files.list(origem)) {
            return arquivos
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".json"))
                    .sorted()
                    .toList();
        }
    }
}
