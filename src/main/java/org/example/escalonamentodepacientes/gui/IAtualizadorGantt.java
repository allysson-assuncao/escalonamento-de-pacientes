package org.example.escalonamentodepacientes.gui;

import org.example.escalonamentodepacientes.model.Medico;
import org.example.escalonamentodepacientes.model.Paciente;

import java.util.List;
import java.util.Queue;

/**
 * Interface de callback para o Simulador se comunicar com a GUI
 * sem acoplamento direto, permitindo a atualização dinâmica do Gantt.
 */
public interface IAtualizadorGantt {

    /**
     * Chamado a cada "tick" do relógio pelo Simulador.
     * @param tempoAtual O tempo atual da simulação.
     * @param medicos O estado atual de todos os médicos.
     * @param filaDeProntos O estado atual da fila de espera.
     */
    void atualizarVisualizacao(int tempoAtual, List<Medico> medicos, Queue<Paciente> filaDeProntos);
}
