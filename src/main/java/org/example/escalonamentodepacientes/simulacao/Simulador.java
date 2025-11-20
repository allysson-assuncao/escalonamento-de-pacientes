package org.example.escalonamentodepacientes.simulacao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.escalonamentodepacientes.enums.AlgoritmoEscalonamento;
import org.example.escalonamentodepacientes.enums.CenarioSimulacao;
import org.example.escalonamentodepacientes.enums.StatusPaciente;
import org.example.escalonamentodepacientes.escalonadores.*;
import org.example.escalonamentodepacientes.model.ConfiguracaoSimulacao;
import org.example.escalonamentodepacientes.model.Medico;
import org.example.escalonamentodepacientes.model.Paciente;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private final CenarioSimulacao cenario;

    private final List<Medico> medicos;
    private final List<Paciente> pacientesNovos; // Pacientes que ainda não chegaram
    private final Queue<Paciente> filaDeProntos;
    private final List<Paciente> pacientesConcluidos;

    private int proximoIdRuntime; // Para IDs únicos na adição de pacientes em tempo de execução

    private int tempoAtual;
    private int totalTrocasContexto;

    public Simulador(ConfiguracaoSimulacao config, IAtualizadorVisual visualizador, CenarioSimulacao cenario) {
        this.config = config;
        this.visualizador = visualizador;
        this.cenario = cenario;
        this.tempoAtual = 0;
        this.totalTrocasContexto = 0;

        this.escalonador = criarEscalonador(config.getAlgoritmo());

        this.medicos = new ArrayList<>();
        for (int i = 0; i < config.getNumeroMedicos(); i++) {
            this.medicos.add(new Medico(i + 1));
        }

        // Copia a lista para não modificar a original
        // CopyOnWriteArrayList para permitir adições durante a iteração
        this.pacientesNovos = new CopyOnWriteArrayList<>(config.getPacientes());

        // Ordena a lista inicial de pacientes por tempo de chagada, processo últil para todos os algoritmos
        this.pacientesNovos.sort(Comparator.comparingInt(Paciente::getTempoChegada));

        this.filaDeProntos = new java.util.concurrent.ConcurrentLinkedQueue<>();
        this.pacientesConcluidos = new CopyOnWriteArrayList<>();

        // Define um ID inicial para pacientes runtime
        this.proximoIdRuntime = 1000 + config.getPacientes().size();
    }

    /**
     * Cria a instância do algoritmo de escalonamento selecionado.
     */
    private IEscalonador criarEscalonador(AlgoritmoEscalonamento alg) {
        return switch (alg) {
            case ROUND_ROBIN -> new EscalonadorRoundRobin();
            case SJF -> new EscalonadorSJF();
            case SRTF -> new EscalonadorSRTF();
            case PRIORIDADE_NP -> new EscalonadorPrioridade();
            default -> throw new IllegalArgumentException("Algoritmo desconhecido: " + alg);
        };
    }

    /**
     * Método público e thread-safe para adicionar pacientes.
     * Chamado pelos botões da TelaSimulacao.
     */
    public synchronized void adicionarPacienteRuntime(Paciente paciente) {
        // Define os dados do paciente no momento da adição
        paciente.setId(proximoIdRuntime++);
        paciente.setTempoChegada(this.tempoAtual + 1); // Chega no próximo tick

        // Adicionar a esta lista é thread-safe
        this.pacientesNovos.add(paciente);

        System.out.println("[T=" + tempoAtual + "] NOVO PACIENTE RUNTIME: " + paciente + " chegará em T=" + paciente.getTempoChegada());
    }

    /**
     * Executa o loop principal da simulação (Relógio Global).
     */
    public void executarSimulacao() {
        System.out.println("Simulação iniciada...");

        // O loop continua enquanto houver pacientes novos, na fila ou sendo executados (considerando os possivelmente adicionados em tempo real)
        // ou continua enquanto houver pacientes na fila, médicos ocupados, ou pacientes pra chegar
        while (pacientesConcluidos.size() < config.getPacientes().size() + (proximoIdRuntime - (1000 + config.getPacientes().size()))
                || !filaDeProntos.isEmpty()
                || medicos.stream().anyMatch(m -> !m.estaOcioso())
                || !pacientesNovos.isEmpty()) {
            System.out.println("Executando fluxo principal, tempo: " + this.tempoAtual);

            // --- FASE 1: Processamento e tratamentos específicos (Médicos trabalham) ---
            processarAtendimentos();

            // --- FASE 2: Chegada dos pacientes ---
            processarChegadas();

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

        // Reverte o último incremento, que não foi processado
        this.tempoAtual--;
        System.out.println("Simulação concluída no tempo: " + (this.tempoAtual));
        calcularMetricasFinais();
    }

    private void processarChegadas() {
        // Lista temporária para armazenar pacientes que chegaram neste tick
        List<Paciente> pacientesQueChegaram = new ArrayList<>();

        // 1. Itera sobre a lista (sem modificar) para encontrar quem chegou
        for (Paciente p : pacientesNovos) {
            if (p.getTempoChegada() <= tempoAtual) {
                p.setStatus(StatusPaciente.PRONTO);
                filaDeProntos.add(p);
                pacientesQueChegaram.add(p); // Adiciona na lista temporária

                // Se for SRTF, uma chegada pode causar preempção
                 if (config.getAlgoritmo() == AlgoritmoEscalonamento.SRTF) {
                    verificarPreempcaoSRTF(p);
                }
            }
        }

        // 2. Remove todos os pacientes que chegaram da lista principal.
        // Esta operação (removeAll) é thread-safe no CopyOnWriteArrayList.
        if (!pacientesQueChegaram.isEmpty()) {
            pacientesNovos.removeAll(pacientesQueChegaram);
        }
    }

    private void verificarPreempcaoSRTF(Paciente pacienteNovo) {
        // Tenta encontrar um médico que esteja atendendo um paciente com duração maior do que o recem chegado
        for (Medico medico : medicos) {
            if (!medico.estaOcioso() && pacienteNovo.getTempoRestante() < medico.getPacienteAtual().getTempoRestante()) {
                System.out.println("[T=" + tempoAtual + "] Preempção SRTF: " + pacienteNovo + " (TR=" + pacienteNovo.getTempoRestante() +
                        ") está preemptando " + medico.getPacienteAtual() + " (TR=" + medico.getPacienteAtual().getTempoRestante() + ")");

                // 1. Libera o paciente antigo (que estava sendo executado pelo médico)
                Paciente pacienteAntigo = medico.liberarMedico();

                // 2. Coloca o paciente antigo de volta na fila de prontos
                pacienteAntigo.setStatus(StatusPaciente.PRONTO);
                filaDeProntos.add(pacienteAntigo);

                // 3. Remove o paciente novo da fila de prontos
                filaDeProntos.remove(pacienteNovo);

                // 4. Atribui o paciente novo ao médico
                medico.atenderPaciente(pacienteNovo);

                // 5. Contabiliza a troca de contexto
                totalTrocasContexto++;

                // 6. Para o loop, para evitar que o paciente interrompa o atendimento de outro médico
                break;
            }
        }
    }

    private void processarAtendimentos() {
        for (Medico medico : medicos) {
            if (medico.estaOcioso()) {
                continue; // Próximo médico
            }

            System.out.println("[T=" + tempoAtual + "] ATENDIMENTO: " + medico.getPacienteAtual() +
                           " (TR antes=" + medico.getPacienteAtual().getTempoRestante() + ")");

            // Médico trabalha (RR: incrementa o contador interno de quantum)
            medico.executaTickConsulta();
            Paciente paciente = medico.getPacienteAtual();

            // 1. Verifica se o paciente terminou
            if (paciente.getStatus() == StatusPaciente.CONCLUIDO) {
                paciente.finalizarTurnaround(tempoAtual);
                pacientesConcluidos.add(paciente);
                medico.liberarMedico();
                // 2. Se não terminou, verifica preempção por Quantum (RR)
            } else if (config.getAlgoritmo() == AlgoritmoEscalonamento.ROUND_ROBIN) {
                int quantumDefinido = config.getQuantum();

                // Verifica se o atendimento atual do médico estourou o quantum
                if (medico.getTempoNoAtendimentoAtual() >= quantumDefinido) {

                    System.out.println("[T=" + tempoAtual + "] PREEMPÇÃO RR: " + paciente + " (Quantum " + quantumDefinido + " estourou)");

                    Paciente pacientePreemptado = medico.liberarMedico();

                    // Coloca o paciente de volta no fim da fila de prontos
                    pacientePreemptado.setStatus(StatusPaciente.PRONTO);
                    filaDeProntos.add(pacientePreemptado);

                    /*totalTrocasContexto++;*/
                }
            }
        }
    }


    private void atribuirTrabalhoOcioso() {
        for (Medico medico : medicos) {
            if (medico.estaOcioso() && !filaDeProntos.isEmpty()) {
                // Pede ao escalonador o próximo paciente
                Paciente proximo = escalonador.selecionarProximo(filaDeProntos);

                if (proximo != null) {
                    // Remove da fila e atribui ao médico
                    filaDeProntos.remove(proximo);
                    medico.atenderPaciente(proximo);

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

        int totalPacientes = Math.max(pacientesConcluidos.size(), 1); // evita divisão por 0

        double tempoMedioEspera = somaTemposEspera / totalPacientes;
        double tempoMedioExecucao = somaTemposExecucao / totalPacientes;

        double somaTempoOcupadoMedicos = medicos.stream()
                .mapToDouble(Medico::getTempoOcupado)
                .sum();

        double tempoTotalSimulacao = Math.max(this.tempoAtual, 1);

        double utilizacaoMediaCPU = (somaTempoOcupadoMedicos / (tempoTotalSimulacao * medicos.size())) * 100.0;

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
