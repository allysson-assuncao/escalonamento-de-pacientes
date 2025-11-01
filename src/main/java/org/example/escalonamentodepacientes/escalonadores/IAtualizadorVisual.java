package org.example.escalonamentodepacientes.escalonadores;

import org.example.escalonamentodepacientes.model.Medico;
import org.example.escalonamentodepacientes.model.Paciente;

import java.util.List;
import java.util.Queue;

/**
 * Interface de callback para o Simulador se comunicar com a GUI
 * sem acoplamento direto, permitindo a atualização dinâmica.
 */
public interface IAtualizadorVisual {

    /**
     * Chamado a cada "tick" do relógio pelo Simulador.
     * @param tempoAtual O tempo atual da simulação.
     * @param medicos O estado atual de todos os médicos.
     * @param filaDeProntos O estado atual da fila de pacientes em espera.
     */
    void atualizarVisualizacao(int tempoAtual, List<Medico> medicos, Queue<Paciente> filaDeProntos);

    /**
     * Chamado ao final da simulação para exibir as métricas.
     * @param resultados O texto formatado com os resultados.
     */
    void exibirMetricasFinais(String resultados);
}
