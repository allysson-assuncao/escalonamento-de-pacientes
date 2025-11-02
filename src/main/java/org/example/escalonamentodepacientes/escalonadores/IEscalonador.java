package org.example.escalonamentodepacientes.escalonadores;

import org.example.escalonamentodepacientes.model.Paciente;

import java.util.Queue;

/**
 * Interface que define a estrutura de todos os algoritmos de escalonamento.
 */
public interface IEscalonador {

    /**
     * Decide qual é o próximo paciente a ser executado da fila de prontos.
     *
     * @param filaDeProntos A fila de pacientes no estado PRONTO.
     * @param tempoAtual O "tick" atual do relógio global.
     * @return O paciente selecionado.
     */
    Paciente selecionarProximo(Queue<Paciente> filaDeProntos, int tempoAtual);
}
