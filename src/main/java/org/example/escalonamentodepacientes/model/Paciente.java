package org.example.escalonamentodepacientes.model;

import org.example.escalonamentodepacientes.enums.StatusPaciente;

/**
 * Representa um Paciente (Processo) no sistema.
 */
public class Paciente {

    // --- Dados de Entrada (Configuração) ---
    private int id;
    private int tempoChegada;
    private int tempoDuracao; // Burst Time
    private int prioridade;   // Quanto menor maior

    // --- Dados de Simulação (Controle) ---
    private StatusPaciente status;
    private int tempoRestante;

    // --- Dados de Métrica (Resultado) ---
    private int tempoEsperaTotal;
    private int tempoExecucaoTotal; // Turnaround Time

    public Paciente(int id, int tempoChegada, int tempoDuracao, int prioridade) {
        this.id = id;
        this.tempoChegada = tempoChegada;
        this.tempoDuracao = tempoDuracao;
        this.prioridade = prioridade;

        // Inicialização
        this.status = StatusPaciente.NOVO;
        this.tempoRestante = this.tempoDuracao;
        this.tempoEsperaTotal = 0;
        this.tempoExecucaoTotal = 0;
    }

}
