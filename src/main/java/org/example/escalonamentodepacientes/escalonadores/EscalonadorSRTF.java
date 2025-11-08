package org.example.escalonamentodepacientes.escalonadores;

import org.example.escalonamentodepacientes.model.Paciente;

import java.util.Queue;

public class EscalonadorSRTF implements IEscalonador {

    @Override
    public Paciente selecionarProximo(Queue<Paciente> filaDeProntos, int tempoAtual) {

        // Verifica a a fila esta vaziia.
        if (filaDeProntos.isEmpty()) {
            return null;
        }

        //  o menor tempo é o maior valor.
        int menorTempoRestante = Integer.MAX_VALUE;
        Paciente pacienteEscolhido = null;

        // Passo por todos pacientes na fila de prontos.
        for (Paciente pacienteAtual : filaDeProntos) {
            // Comparamos o tempo restante.
            if (pacienteAtual.getTempoRestante() < menorTempoRestante) {

                // se o tempo restante for menor. ele passa a ser o paciente.
                menorTempoRestante = pacienteAtual.getTempoRestante();
                pacienteEscolhido = pacienteAtual;
            }
        }
        // Retorna o paciente que teve o menor tempo RESTANTE.
        return pacienteEscolhido;
    }
}
