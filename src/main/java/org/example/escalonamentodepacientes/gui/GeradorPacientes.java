package org.example.escalonamentodepacientes.gui;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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

    /**
     * Gera uma lista de pacientes com valores aleatórios.
     */
    public static List<Paciente> gerarListaAleatoria(int quantidade) {
        List<Paciente> lista = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < quantidade; i++) {
            int id = i + 1;
            int tempoChegada = rand.nextInt(quantidade * 2);
            int tempoDuracao = rand.nextInt(0,11); // Duração de 1 a 10
            int prioridade = rand.nextInt(-10,11); // Prioridade de -10 a 10
            lista.add(new Paciente(id, tempoChegada, tempoDuracao, prioridade));
        }
        return lista;
    }

    /**
     * Retorna uma lista pré-definida de pacientes com base em um cenário.
     * @param nomeCenario O cenário desejado
     */
    public static List<Paciente> getListaPredefinida(String nomeCenario) {
        List<Paciente> lista = new ArrayList<>();

        switch (nomeCenario) {
            case "CenarioSlides" -> {
                // Usando os mesmos dados dos exemplos dos slides da aula 07
                // Adaptação das prioridade para decrescente
                lista.add(new Paciente(1, 0, 5, 4));
                lista.add(new Paciente(2, 0, 2, 3));
                lista.add(new Paciente(3, 1, 4, 5));
                lista.add(new Paciente(4, 3, 1, 2));
                lista.add(new Paciente(5, 5, 2, 1));
            }
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
                lista.add(new Paciente(1, 0, 5, 3)); // Chega primeiro, baixa prio
                lista.add(new Paciente(2, 1, 3, 1)); // Chega depois, alta prio
                lista.add(new Paciente(3, 2, 4, 2));
            }
            case "CenarioSRTF" -> {
                // Ideal para ver preempção
                lista.add(new Paciente(1, 0, 10, 2)); // Longo, chega primeiro
                lista.add(new Paciente(2, 2, 2, 1));  // Curto, chega e deve preeptar P1
            }
            case null, default -> {
                // Cenário Padrão
                lista.add(new Paciente(1, 0, 7, 1)); // Crítico
                lista.add(new Paciente(2, 1, 3, 3));
                lista.add(new Paciente(3, 2, 5, 5)); // Baixa prioridade
            }
        }

        return lista;
    }

}
