package org.example.escalonamentodepacientes.simulacao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.escalonamentodepacientes.enums.CenarioSimulacao;
import org.example.escalonamentodepacientes.model.Paciente;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe utilitária para gerar listas de pacientes,
 * seja aleatoriamente ou usando cenários pré-definidos.
 */
@Getter
@Setter
@ToString
public class GeradorPacientes {

    private static final Random rand = new Random();

    /**
     * Gera uma lista de pacientes com valores aleatórios.
     */
    public static List<Paciente> gerarListaAleatoria(int quantidade) {
        List<Paciente> lista = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < quantidade; i++) {
            int id = i + 1;
            int tempoChegada = rand.nextInt(quantidade * 2);

            // CORREÇÃO: rand.nextInt(1, 11)
            // Isso gera um número de 1 (inclusivo) a 11 (exclusivo), ou seja, 1 a 10.
            int tempoDuracao = rand.nextInt(1, 11);

            int prioridade = rand.nextInt(-10, 11);
            lista.add(new Paciente(id, tempoChegada, tempoDuracao, prioridade));
        }
        return lista;
    }

    /**
     * Retorna a lista de pacientes para um Cenário específico.
     */
    public static List<Paciente> getListaCenario(CenarioSimulacao cenario) {
        return switch (cenario) {
            case CENARIO_1_EMERGENCIA -> getCenario1Emergencia();
            case CENARIO_2_PLANTAO_LOTADO -> getCenario2PlantaoLotado();
            case CENARIO_3_HOSPITAL_MODERNO -> getCenario3HospitalModerno();
            default -> new ArrayList<>(); // Retorna lista vazia para 'Customizado'
        };
    }

    // "Poucos, mas com níveis de urgência (Prioridade) muito diferentes."
    private static List<Paciente> getCenario1Emergencia() {
        List<Paciente> lista = new ArrayList<>();
        lista.add(new Paciente(1, 0, 8, 1)); // Crítico, chega primeiro
        lista.add(new Paciente(2, 1, 3, 5)); // Baixa prioridade
        lista.add(new Paciente(3, 2, 4, 2)); // Prioridade média
        lista.add(new Paciente(4, 3, 5, 1)); // Crítico, chega depois
        lista.add(new Paciente(5, 5, 2, 10)); // Prioridade muito baixa
        return lista;
    }

    // "Muitos, chegando continuamente, com tempos de atendimento variados."
    /*private static List<Paciente> getCenario2PlantaoLotado() {
        List<Paciente> lista = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            int id = i + 1;
            int tempoChegada = i * 2; // Chegando continuamente
            int tempoDuracao = rand.nextInt(3, 12); // Tempos variados
            int prioridade = rand.nextInt(3, 8); // Prioridades médias (range restrito)
            lista.add(new Paciente(id, tempoChegada, tempoDuracao, prioridade));
        }
        return lista;
    }*/

    // "5 pacientes, chegando continuamente, com tempos de atendimento variados e prioridades médias."
    private static List<Paciente> getCenario2PlantaoLotado() {
        List<Paciente> lista = new ArrayList<>();
        lista.add(new Paciente(1, 0, 7, 3));
        lista.add(new Paciente(2, 2, 4, 5));
        lista.add(new Paciente(3, 3, 8, 7));
        lista.add(new Paciente(4, 5, 3, 6));
        lista.add(new Paciente(5, 6, 5, 4));
        return lista;
    }

    // "Chegando de forma aleatória, com diferentes Burst Times e Prioridades."
    /*private static List<Paciente> getCenario3HospitalModerno() {
        List<Paciente> lista = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int id = i + 1;
            int tempoChegada = rand.nextInt(30); // Chegadas muito aleatórias
            int tempoDuracao = rand.nextInt(1, 15); // Burst Times diferentes
            int prioridade = rand.nextInt(1, 11); // Prioridades diferentes (range total)
            lista.add(new Paciente(id, tempoChegada, tempoDuracao, prioridade));
        }
        return lista;
    }*/

    // "5 pacientes, chegando de forma aleatória, com diferentes Burst Times e Prioridades."
    private static List<Paciente> getCenario3HospitalModerno() {
        List<Paciente> lista = new ArrayList<>();
        lista.add(new Paciente(1, 0, 3, 8));
        lista.add(new Paciente(2, 5, 10, 1));
        lista.add(new Paciente(3, 7, 2, 5));
        lista.add(new Paciente(4, 8, 6, 3));
        lista.add(new Paciente(5, 12, 4, 2));
        return lista;
    }

    /**
     * Gera um único paciente para adição em tempo de execução com base no contexto do cenário.
     *
     * @param cenario O cenário atual (para definir o range dos valores)
     * @return Um novo Paciente (ID e TempoChegada serão definidos pelo Simulador)
     */
    public static Paciente gerarPacienteRuntime(CenarioSimulacao cenario) {
        int duracao;
        int prioridade;

        switch (cenario) {
            case CENARIO_1_EMERGENCIA:
                // Gera principalmente pacientes de alta prioridade
                duracao = rand.nextInt(3, 8);
                prioridade = rand.nextInt(1, 4); // Range 1-3 (Alta prioridade)
                break;
            case CENARIO_2_PLANTAO_LOTADO:
                // Gera pacientes "médios"
                duracao = rand.nextInt(2, 12);
                prioridade = rand.nextInt(3, 8); // Range 3-7 (Média prioridade)
                break;
            case CENARIO_3_HOSPITAL_MODERNO:
            default:
                // Gera qualquer tipo de paciente
                duracao = rand.nextInt(1, 15);
                prioridade = rand.nextInt(1, 11); // Range 1-10 (Prioridade qualquer)
                break;
        }
        // ID e TempoChegada (0) são placeholders
        return new Paciente(0, 0, duracao, prioridade);
    }

    /**
     * Retorna uma lista pré-definida de pacientes com base em um cenário.
     *
     * @param nomeCenario O cenário desejado
     */
    public static List<Paciente> getListaPredefinida(String nomeCenario) {
        List<Paciente> lista = new ArrayList<>();

        switch (nomeCenario) {
            case "CenarioSJF" -> {
                // Todos chegam juntos, mas com durações diferentes.
                // Ideal para destacar o funcionamento do SJF.
                lista.add(new Paciente(1, 0, 8, 3));
                lista.add(new Paciente(2, 0, 3, 2));
                lista.add(new Paciente(3, 0, 5, 1));
                lista.add(new Paciente(4, 0, 2, 2));
                lista.add(new Paciente(5, 0, 2, 3));
                lista.add(new Paciente(6, 0, 4, 1));
            }
            case "CenarioPrioridade" -> {
                // Chegadas diferentes, mas prioridades claras.
                lista.add(new Paciente(1, 0, 8, 1));
                lista.add(new Paciente(2, 1, 3, 5));
                lista.add(new Paciente(3, 2, 4, 2));
                lista.add(new Paciente(4, 3, 5, 1));
                lista.add(new Paciente(5, 5, 2, 10));
            }
            case "CenarioSRTF" -> {
                // Ideal para ver preempção
                lista.add(new Paciente(1, 0, 5, 2));
                lista.add(new Paciente(2, 0, 2, 3));
                lista.add(new Paciente(3, 1, 4, 1));
                lista.add(new Paciente(4, 3, 1, 4));
                lista.add(new Paciente(5, 5, 2, 5));
            }
            case null, default -> {
                // Usando os mesmos dados dos exemplos dos slides da aula 07
                // Adaptação das prioridade para decrescente
                lista.add(new Paciente(1, 0, 5, 4));
                lista.add(new Paciente(2, 0, 2, 3));
                lista.add(new Paciente(3, 1, 4, 5));
                lista.add(new Paciente(4, 3, 1, 2));
                lista.add(new Paciente(5, 5, 2, 1));
            }
        }

        return lista;
    }

}
