package org.example.escalonamentodepacientes.escalonadores;

import lombok.Getter;
import org.example.escalonamentodepacientes.model.Paciente;

import java.util.*;

/**
 * Implementa o Escalonamento por Prioridade Não-Preemptivo (Cooperativo).
 * A lógica de seleção é baseada na prioridade (e em FIFO em caso de empate).
 */
@Getter
public class EscalonadorPrioridade implements IEscalonador {

    @Override
    public Paciente selecionarProximo(Queue<Paciente> filaDeProntos, int tempoAtual) {
        Paciente paciente = null;

        for (Paciente p : filaDeProntos) {
            if (p == null) continue;
            if (paciente == null || p.getPrioridade() < paciente.getPrioridade()) { // O "<" garante o FIFO
                paciente = p;
            }
        }

        return paciente;
    }
}