package org.example.escalonamentodepacientes.model;

import lombok.Getter;
import lombok.Setter;
import org.example.escalonamentodepacientes.enums.StatusPaciente;

/**
 * Representa um Paciente (Processo) no sistema.
 */
@Getter
@Setter
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

    /**
     * Incrementa o tempo de espera do paciente.
     * Chamado a cada "tick" do relógio enquanto ele está na fila de PRONTO.
     */
    public void incrementarTempoEspera() {
        if (this.status == StatusPaciente.PRONTO) {
            this.tempoEsperaTotal++;
        }
    }

    /**
     * Processa um "tick" de atendimento médico.
     */
    public void executarTick() {
        if (this.status == StatusPaciente.EXECUTANDO) {
            this.tempoRestante--;
            if (this.tempoRestante <= 0) {
                this.status = StatusPaciente.CONCLUIDO;
            }
        }
    }

    /**
     * Define o tempo de execução total (Turnaround)
     * quando o paciente é concluído.
     */
    public void finalizarTurnaround(int tempoAtual) {
        // Turnaround = Tempo de Conclusão - Tempo de Chegada
        this.tempoExecucaoTotal = (tempoAtual + 1) - this.tempoChegada;
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + id +
                ", tempoChegada=" + tempoChegada +
                ", tempoDuracao=" + tempoDuracao +
                ", prioridade=" + prioridade +
                '}';
    }
}
