# projetoRPA – Cadastro automatizado de médicos (Trabalho 2 – TSIN35D)

Robô (RPA) em Java + Selenium que lê os médicos dos arquivos JSON e, **pela interface web**
da aplicação *Clínica Médica*, faz todo o processo pedido no enunciado:

1. **Habilitação** – entra como `admin` e habilita o CPF de cada médico (`CRM-CRM-UF`).
2. **Cadastro** – preenche o formulário "Cadastro de Médico" para cada um
   (e-mail `PRIMEIRO_NOME-CRM-UF@medicos.com`, CRM `CRM-UF`, senha `CRM-UF`, especialidades do JSON).
3. **Verificação** – entra como um médico, lê a "Lista de Médicos" e confere se todos os CPFs da fonte
   estão lá; quem estiver faltando é recadastrado.
4. **Remoção** – cada médico faz login com as próprias credenciais e clica em "Remover" na própria linha.

No fim gera `relatorio-<modo>-<data>.csv` com o status de cada médico em cada etapa.

## Estrutura

```
br.edu.utfpr.td.tsi
├── App                      ponto de entrada, lê argumentos e monta tudo
├── modelo                   MedicoFonte (JSON), ArquivoMedicos, Medico (dados do formulário), Especialidade
├── leitura                  LeitorJsonMedicos – lê um .json ou todos de uma pasta (Jackson)
├── conversao                ConversorMedico – aplica os padrões CPF/CRM/e-mail/senha e mapeia especialidades
├── paginas                  Page Objects (uma classe por tela): Login, HabilitarCadastro,
│                            CadastroMedico, ListagemMedicos, Principal
├── robo                     RoboClinicaMedica (orquestra as 4 etapas, com novas tentativas), Navegador, Modo
└── relatorio                Relatorio – status por médico/etapa, resumo e CSV
```

Pontos de robustez:
- **Idempotente**: antes de habilitar, confere se o CPF já está na lista (a aplicação aceitaria duplicado);
  "CPF já cadastrado" conta como sucesso. Pode rodar de novo sem bagunçar o sistema.
- **Novas tentativas**: cada operação é tentada até 3 vezes.
- **Conferência**: compara a lista de habilitados e a listagem de médicos com a fonte de dados.

## Como rodar

Pré-requisitos: JDK 17+, Google Chrome. O chromedriver é baixado automaticamente pelo Selenium.

1. Subir a aplicação do professor (precisa de **Java 21+** e MongoDB em `localhost:27017`):
   ```
   cd clinica-medica
   java -jar clinica.medica-4.0.0.jar --spring.mongodb.host=localhost --spring.mongodb.port=27017 --spring.mongodb.database=clinica-medica
   ```
   (os parâmetros corrigem o `application.properties`, que está com duas linhas grudadas.)
   Fica em `http://localhost:8080/clinica.medica`.

2. Gerar o JAR do robô: `mvn package` → `target/projetoRPA.jar`.

3. Executar (da pasta `projetoRPA`, ele acha `../dados` sozinho):
   ```
   java -jar target/projetoRPA.jar                      # processo completo, com janela do Chrome
   java -jar target/projetoRPA.jar --limite=5           # só 5 médicos (bom para a demonstração)
   java -jar target/projetoRPA.jar --modo=cadastrar     # só habilita + cadastra + verifica
   java -jar target/projetoRPA.jar --modo=remover       # só remove
   java -jar target/projetoRPA.jar --headless           # sem abrir janela (mais rápido)
   java -jar target/projetoRPA.jar --url=http://lds.td.utfpr.edu.br:9096/clinica.medica
   java -jar target/projetoRPA.jar --ajuda
   ```
