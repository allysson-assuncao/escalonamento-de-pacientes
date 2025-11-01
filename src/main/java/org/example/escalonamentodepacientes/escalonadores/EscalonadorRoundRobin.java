package org.example.escalonamentodepacientes.escalonadores;

import lombok.Getter;
import org.example.escalonamentodepacientes.model.Paciente;

import java.util.Queue;

/**
 * Implementa o Round-Robin (Preemptivo).
 * A preempção (quantum) é tratada no Simulador.
 */

@Getter
public class EscalonadorRoundRobin implements IEscalonador {

    // Getter para o Simulador saber o quantum
    private final int quantum;

    public EscalonadorRoundRobin(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public Paciente selecionarProximo(Queue<Paciente> filaDeProntos, int tempoAtual) {
        // Comportamento temporário para testar (pega o primeiro da fila)
        return filaDeProntos.peek();
    }

}
