package br.edu.utfpr.td.tsi.conversao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import br.edu.utfpr.td.tsi.modelo.Especialidade;
import br.edu.utfpr.td.tsi.modelo.Medico;
import br.edu.utfpr.td.tsi.modelo.MedicoFonte;

class ConversorMedicoTest {

    private final ConversorMedico conversor = new ConversorMedico();

    private static MedicoFonte fonte(String nome, String crm, String uf, String especialidade) {
        MedicoFonte f = new MedicoFonte();
        f.setNome(nome);
        f.setCrm(crm);
        f.setUf(uf);
        f.setEspecialidade(especialidade);
        return f;
    }

    @Test
    void aplicaPadroesDoEnunciado() {
        Medico m = conversor.converter(fonte("AALEC RINHEL SOUZA FERREIRA DA SILVA", "55775", "PR", null));
        assertEquals("55775-55775-PR", m.getCpf());
        assertEquals("55775-PR", m.getCrm());
        assertEquals("55775-PR", m.getSenha());
        assertEquals("AALEC-55775-PR@medicos.com", m.getEmail());
        assertTrue(m.getEspecialidades().isEmpty());
    }

    @Test
    void emailSemAcentos() {
        Medico m = conversor.converter(fonte("JOSÉ ÂNGELO", "1", "sp", null));
        assertEquals("JOSE-1-SP@medicos.com", m.getEmail());
    }

    @Test
    void mapeiaEspecialidadesIgnorandoAreasDeAtuacao() {
        assertEquals(EnumSet.of(Especialidade.PEDIATRIA), ConversorMedico.especialidades(
                "&PEDIATRIA - RQE N.: 4805 (Áreas de atuação: Neurologia Pediátrica - RQE N.: 1)"));
        assertEquals(EnumSet.of(Especialidade.CARDIOLOGIA, Especialidade.CLINICA_MEDICA), ConversorMedico.especialidades(
                "&CARDIOLOGIA - RQE N.: 2833& CLÍNICA MÉDICA - RQE N.: 6801"));
        assertEquals(EnumSet.of(Especialidade.GINECOLOGIA, Especialidade.ORTOPEDIA), ConversorMedico.especialidades(
                "&GINECOLOGIA E OBSTETRÍCIA - RQE N.: 1&ORTOPEDIA E TRAUMATOLOGIA - RQE N.: 2"));
        assertTrue(ConversorMedico.especialidades("&ANESTESIOLOGIA - RQE N.: 3700").isEmpty());
    }
}
