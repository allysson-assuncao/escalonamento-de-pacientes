package org.example.escalonamentodepacientes.simulacao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.escalonamentodepacientes.enums.AlgoritmoEscalonamento;
import org.example.escalonamentodepacientes.enums.StatusPaciente;
import org.example.escalonamentodepacientes.escalonadores.EscalonadorRoundRobin;
import org.example.escalonamentodepacientes.escalonadores.EscalonadorSJF;
import org.example.escalonamentodepacientes.escalonadores.IAtualizadorVisual;
import org.example.escalonamentodepacientes.escalonadores.IEscalonador;
import org.example.escalonamentodepacientes.model.ConfiguracaoSimulacao;
import org.example.escalonamentodepacientes.model.Medico;
import org.example.escalonamentodepacientes.model.Paciente;

import java.util.*;

/**
 * O fluxo central da simulação. Gerencia o "Relógio Global",
 * a fila de prontos, os médicos e o ciclo de vida dos pacientes.
 */

@Getter
@Setter
@ToString
public class Simulador {

    private ConfiguracaoSimulacao config;
    private IEscalonador escalonador;
    private IAtualizadorVisual visualizador; // Interface para atualizar o Gantt

    private List<Medico> medicos;
    private List<Paciente> pacientesNovos; // Pacientes que ainda não chegaram
    private Queue<Paciente> filaDeProntos;
    private List<Paciente> pacientesConcluidos;

    private int tempoAtual;
    private int totalTrocasContexto;
    private int quantumAtual; // Contador para Round-Robin

    public Simulador(ConfiguracaoSimulacao config, IAtualizadorVisual visualizador) {
        this.config = config;
        this.visualizador = visualizador;
        this.tempoAtual = 0;
        this.totalTrocasContexto = 0;

        this.escalonador = criarEscalonador(config.getAlgoritmo());

        this.medicos = new ArrayList<>();
        for (int i = 0; i < config.getNumeroMedicos(); i++) {
            this.medicos.add(new Medico(i + 1));
        }

        // Copia a lista para não modificar a original
        this.pacientesNovos = new ArrayList<>(config.getPacientes());
        // Depois precisamos ordenar a lista de pacientes por ordem de chegada (isso é útil para todos os algoritmos)
        // this.pacientesNovos.sort(...);

        this.filaDeProntos = new LinkedList<>();
        this.pacientesConcluidos = new ArrayList<>();
    }

    /**
     * Cria a instância do algoritmo de escalonamento selecionado.
     */
    private IEscalonador criarEscalonador(AlgoritmoEscalonamento alg) {
        switch (alg) {
            case ROUND_ROBIN:
                this.quantumAtual = config.getQuantum();
                return new EscalonadorRoundRobin(config.getQuantum());
            case SJF:
                return new EscalonadorSJF();
            case SRTF:
                // return new EscalonadorSRTF();
            case PRIORIDADE_NP:
                // return new EscalonadorPrioridade();
            default:
                throw new IllegalArgumentException("Algoritmo desconhecido: " + alg);
        }
    }

    /**
     * Executa o loop principal da simulação (Relógio Global).
     */
    public void executarSimulacao() {
        System.out.println("Simulação iniciada...");

        // O loop continua enquanto houver pacientes novos, na fila ou sendo executados
        while (pacientesConcluidos.size() < config.getPacientes().size()) {

            // --- FASE 1: Chegada dos pacientes ---
            processarChegadas();

            // --- FASE 2: Processamento e tratamentos específicos (Médicos trabalham) ---
            processarAtendimentos();

            // --- FASE 3: Escalonamento principal (Atribuição das consultas) ---
            atribuirTrabalhoOcioso();

            // --- FASE 4: Espera (Atualiza as métricas de quem está na fila) ---
            atualizarTempoEsperaFila();

            // --- FASE 5: Visualização da passagem de tempo ---
            if (visualizador != null) {
                // Envia o estado atual para a GUI
                visualizador.atualizarVisualizacao(tempoAtual, medicos, filaDeProntos);
            }

            // --- FASE 6: Avança o Relógio ---
            this.tempoAtual++;

            // Pequeno delay para permitir a visualização
            try {
                Thread.sleep(1000); // 1s por "tick" do relógio global
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Simulação concluída no tempo: " + (this.tempoAtual));
        calcularMetricasFinais();
    }

    private void processarChegadas() {
        // Uso do Iterator para permitir remoção segura
        Iterator<Paciente> it = pacientesNovos.iterator();
        while (it.hasNext()) {
            Paciente p = it.next();
            if (p.getTempoChegada() == tempoAtual) {
                p.setStatus(StatusPaciente.PRONTO);
                filaDeProntos.add(p);
                it.remove(); // Remove da lista de 'novos'

                // Se for SRTF, uma chegada pode causar preempção
                /*if (config.getAlgoritmo() == AlgoritmoEscalonamento.SRTF) {
                    verificarPreempcaoSRTF(p);
                }*/
            } else if (p.getTempoChegada() > tempoAtual) {
                // Como a lista está ordenada, podemos parar de procurar
                break;
            }
        }
    }

    private void verificarPreempcaoSRTF(Paciente pacienteNovo) {
        // Tenta encontrar um médico que esteja atendendo um paciente com duração maior do que o recem chegado
        // Deve realizar a substituição em caso verdadeiro
    }

    private void processarAtendimentos() {
        for (Medico medico : medicos) {
            if (medico.estaOcioso()) {
                continue; // Próximo médico
            }

            // Médico trabalha
            medico.executaTickConsulta();
            Paciente paciente = medico.getPacienteAtual();

            // 1. Verifica se o paciente terminou
            if (paciente.getStatus() == StatusPaciente.CONCLUIDO) {
                paciente.finalizarTurnaround(tempoAtual);
                pacientesConcluidos.add(paciente);
                medico.liberarMedico();

                // 2. Verifica preempção por Quantum (Round-Robin)
            } else if (config.getAlgoritmo() == AlgoritmoEscalonamento.ROUND_ROBIN) {
                // Caso o quantum do paciente atual tenha acabado seguir com a lógica do RR
                // Trocar o paciente atendido e adicionar o anterior novamente a lista caso não tenha sido concluido
            }
        }
    }

    private void atribuirTrabalhoOcioso() {
        for (Medico medico : medicos) {
            if (medico.estaOcioso() && !filaDeProntos.isEmpty()) {
                // Pede ao escalonador o próximo paciente
                Paciente proximo = escalonador.selecionarProximo(filaDeProntos, tempoAtual);

                if (proximo != null) {
                    // Remove da fila e atribui ao médico
                    filaDeProntos.remove(proximo);
                    medico.atenderPaciente(proximo);

                    // Reseta o contador do Quantum para este atendimento (RR)
                    if (config.getAlgoritmo() == AlgoritmoEscalonamento.ROUND_ROBIN) {
                        this.quantumAtual = config.getQuantum();
                    }

                    // Cada novo atendimento corresponde a uma troca de contexo para aquele "Núcleo"
                    totalTrocasContexto++;
                }
            }
        }
    }

    private void atualizarTempoEsperaFila() {
        // Incrementa o tempo de espera de todos que estão na fila
        for (Paciente p : filaDeProntos) {
            p.incrementarTempoEspera();
        }
    }

    private void calcularMetricasFinais() {
        double somaTemposEspera = 0;
        double somaTemposExecucao = 0;

        for (Paciente p : pacientesConcluidos) {
            somaTemposEspera += p.getTempoEsperaTotal();
            somaTemposExecucao += p.getTempoExecucaoTotal();
        }

        int totalPacientes = pacientesConcluidos.size();
        double tempoMedioEspera = somaTemposEspera / totalPacientes;
        double tempoMedioExecucao = somaTemposExecucao / totalPacientes;

        // TODO: Efetivar esse cálculo
        double utilizacaoMediaCPU = 0;

        // Formata os resultados
        String resultados = String.format(
                "--- Resultados Finais ---\n" +
                        "Tempo Total de Simulação: %d ticks\n" +
                        "Total de Trocas de Contexto: %d\n" +
                        "\n" +
                        "Tempo Médio de Espera (AWT): %.2f ticks\n" +
                        "Tempo Médio de Execução (Turnaround): %.2f ticks\n" +
                        "Utilização Média dos Médicos (CPU): %.2f%%\n",
                this.tempoAtual,
                this.totalTrocasContexto,
                tempoMedioEspera,
                tempoMedioExecucao,
                utilizacaoMediaCPU
        );

        System.out.println(resultados);
        if (visualizador != null) {
            visualizador.exibirMetricasFinais(resultados);
        }
    }
}
