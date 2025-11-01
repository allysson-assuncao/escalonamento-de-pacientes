package org.example.escalonamentodepacientes.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.escalonamentodepacientes.enums.StatusPaciente;

/**
 * Representa um Paciente (Processo) no sistema.
 */
@Getter
@Setter
@ToString
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

    public void atualizaPaciente(){
        this.tempoExecucaoTotal++;
        if(this.status != StatusPaciente.EXECUTANDO){
            this.tempoEsperaTotal++;
        }
        if(tempoExecucaoTotal - this.tempoEsperaTotal == this.tempoDuracao){
            this.concluirPaciente();
        }
    }

    private void concluirPaciente(){
        this.status = StatusPaciente.CONCLUIDO;
    }

    public void iniciaExecucaoPaciente(){
        this.status = StatusPaciente.EXECUTANDO;
    }

    public void interrompeExecucaoPaciente(){
        this.status = StatusPaciente.ESPERANDO;
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
     * @return true se o paciente concluiu o atendimento, false caso contrário.
     */
    public boolean executarTick() {
        if (this.status == StatusPaciente.EXECUTANDO) {
            this.tempoRestante--;
            if (this.tempoRestante <= 0) {
                this.status = StatusPaciente.CONCLUIDO;
                return true;
            }
        }
        return false;
    }

    /**
     * Define o tempo de execução total (Turnaround)
     * quando o paciente é concluído.
     */
    public void finalizarTurnaround(int tempoAtual) {
        // Turnaround = Tempo de Conclusão - Tempo de Chegada
        this.tempoExecucaoTotal = (tempoAtual + 1) - this.tempoChegada;
    }

}
