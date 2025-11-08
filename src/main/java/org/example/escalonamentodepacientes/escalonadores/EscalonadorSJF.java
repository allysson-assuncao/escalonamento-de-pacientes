package org.example.escalonamentodepacientes.escalonadores;

import org.example.escalonamentodepacientes.model.Paciente;

import java.util.Queue;

/**
 * Implementa o Shortest Job First (SJF) Não-Preemptivo.
 */
public class EscalonadorSJF implements IEscalonador {
    @Override
    public Paciente selecionarProximo(Queue<Paciente> filaDeProntos, int tempoAtual) {
        // Se a fila está vazia, não há ninguém para escolher.
        if (filaDeProntos.isEmpty()) {
            return null; // Lista vazia
        }
        // Aqui considera menor tempo como maior valor.
        int menorTempoDuracao = Integer.MAX_VALUE;
        Paciente pacienteEscolhido = null;

        // Passa sobre todos os pacientes na fila de prontos
        // porque o paciente com menor tempo pode não ser o primeiro da fila.
        for (Paciente pacienteAtual : filaDeProntos) {

            // Compara o tempo de duração do paciente atual com o menor já encontrado.
            if (pacienteAtual.getTempoDuracao() < menorTempoDuracao) {
                // Se for menor, este passa a ser o nosso paciente de menor tempo.
                menorTempoDuracao = pacienteAtual.getTempoDuracao();
                pacienteEscolhido = pacienteAtual;
            }
        }
        // Retorna o paciente que teve o menor tempo de duração.
        return pacienteEscolhido;
    }
}
