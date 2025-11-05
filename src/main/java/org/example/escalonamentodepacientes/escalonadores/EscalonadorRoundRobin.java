package org.example.escalonamentodepacientes.escalonadores;

import lombok.Getter;
import org.example.escalonamentodepacientes.model.Paciente;

import java.util.Queue;

/**
 * Implementa o Round-Robin (Preemptivo).
 * A lógica de seleção é FCFS (First-Come, First-Served).
 * A preempção (quantum) é tratada no Simulador.
 */
@Getter
public class EscalonadorRoundRobin implements IEscalonador {

    @Override
    public Paciente selecionarProximo(Queue<Paciente> filaDeProntos, int tempoAtual) {
        // Pega o primeiro paciente da lista sem remover, a remoção é feita no Simulador
        return filaDeProntos.peek();
    }

}
