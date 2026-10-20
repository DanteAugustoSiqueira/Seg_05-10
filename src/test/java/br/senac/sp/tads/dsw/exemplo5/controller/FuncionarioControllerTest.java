
package br.senac.sp.tads.dsw.exemplo5.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.senac.sp.tads.dsw.exemplo5.model.Departamento;
import br.senac.sp.tads.dsw.exemplo5.model.Funcionario;
import br.senac.sp.tads.dsw.exemplo5.repository.DepartamentoRepository;
import br.senac.sp.tads.dsw.exemplo5.repository.FuncionarioRepository;

// Para injeção de dependência via @Autowired:
import tools.jackson.databind.ObjectMapper;
// Para injeção de dependência direta:
// import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
public class FuncionarioControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Departamento departamentoPadrao;

    @BeforeEach
    void setUp() {
        Departamento departamento = new Departamento();
        departamento.setNome("Desenvolvimento");
        departamento.setOrcamento(200000.00);
        departamentoPadrao = departamentoRepository.save(departamento);
    }

    @Test
    void deveCriarFuncionarioComSucesso() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Ana Souza");
        funcionario.setDataContratacao(LocalDate.of(2024, 1, 15));
        funcionario.setTrabalhoRemoto(true);
        funcionario.setDepartamento(departamentoPadrao);

        String jsonRequisicao = objectMapper.writeValueAsString(funcionario);

        mockMvc.perform(post("/api/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequisicao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.dataContratacao").value("2024-01-15"))
                .andExpect(jsonPath("$.trabalhoRemoto").value(true))
                .andExpect(jsonPath("$.departamento.id").value(departamentoPadrao.getId()));
    }

    @Test
    void deveRetornarErro400AoCriarFuncionarioComDataFutura() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Bruno Lima");
        funcionario.setDataContratacao(LocalDate.now().plusDays(1));
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);

        String jsonRequisicao = objectMapper.writeValueAsString(funcionario);

        mockMvc.perform(post("/api/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequisicao))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarTodosOsFuncionarios() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Carla Mendes");
        funcionario.setDataContratacao(LocalDate.of(2023, 6, 1));
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);
        funcionarioRepository.save(funcionario);

        mockMvc.perform(get("/api/funcionarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Carla Mendes"));
    }

    @Test
    void deveBuscarFuncionarioPorId() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Diego Alves");
        funcionario.setDataContratacao(LocalDate.of(2022, 10, 3));
        funcionario.setTrabalhoRemoto(true);
        funcionario.setDepartamento(departamentoPadrao);
        Funcionario funcionarioSalvo = funcionarioRepository.save(funcionario);

        mockMvc.perform(get("/api/funcionarios/" + funcionarioSalvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Diego Alves"));
    }

    @Test
    void deveAtualizarFuncionario() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Elisa Costa");
        funcionario.setDataContratacao(LocalDate.of(2021, 4, 12));
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);
        Funcionario funcionarioSalvo = funcionarioRepository.save(funcionario);

        funcionarioSalvo.setNome("Elisa Costa Atualizada");
        funcionarioSalvo.setDataContratacao(LocalDate.of(2022, 5, 20));
        funcionarioSalvo.setTrabalhoRemoto(true);
        String jsonAtualizado = objectMapper.writeValueAsString(funcionarioSalvo);

        mockMvc.perform(put("/api/funcionarios/" + funcionarioSalvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Elisa Costa Atualizada"))
                .andExpect(jsonPath("$.dataContratacao").value("2022-05-20"))
                .andExpect(jsonPath("$.trabalhoRemoto").value(true));
    }

    @Test
    void deveApagarFuncionario() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Fabio Santos");
        funcionario.setDataContratacao(LocalDate.of(2020, 8, 17));
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);
        Funcionario funcionarioSalvo = funcionarioRepository.save(funcionario);

        mockMvc.perform(delete("/api/funcionarios/" + funcionarioSalvo.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/funcionarios/" + funcionarioSalvo.getId()))
                .andExpect(status().isNotFound());
    }
}
