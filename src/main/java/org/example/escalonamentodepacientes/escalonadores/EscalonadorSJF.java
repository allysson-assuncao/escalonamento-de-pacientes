package org.example.escalonamentodepacientes.escalonadores;

import org.example.escalonamentodepacientes.model.Paciente;

import java.util.Queue;

/**
 * Implementa o Shortest Job First (SJF) Não-Preemptivo.
 */
public class EscalonadorSJF implements IEscalonador {

    @Override
    public Paciente selecionarProximo(Queue<Paciente> filaDeProntos, int tempoAtual) {
        // Deve iterar sobre a fila de pacientes e selcionar o com menor 'tempoDuracao' (Burst Time)

        // Comportamento temporário para testar
        return filaDeProntos.peek();
    }
}
