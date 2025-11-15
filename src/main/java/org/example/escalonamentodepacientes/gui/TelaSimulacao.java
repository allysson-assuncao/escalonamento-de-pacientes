package org.example.escalonamentodepacientes.gui;

import lombok.Setter;
import org.example.escalonamentodepacientes.enums.AlgoritmoEscalonamento;
import org.example.escalonamentodepacientes.enums.CenarioSimulacao;
import org.example.escalonamentodepacientes.escalonadores.IAtualizadorVisual;
import org.example.escalonamentodepacientes.model.Medico;
import org.example.escalonamentodepacientes.model.Paciente;
import org.example.escalonamentodepacientes.simulacao.GeradorPacientes;
import org.example.escalonamentodepacientes.simulacao.Simulador;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Setter
public class TelaSimulacao extends JFrame implements IAtualizadorVisual {

    private JLabel labelTempoAtual;
    private JTextArea areaFilaProntos;
    private JTextArea areaResultados;
    private List<JLabel> labelsStatusMedicos;
    private PainelGantt painelGantt;
    private JScrollPane scrollGantt;

    // Uma cópia do PainelGantt para ser exibida no pop-up final. GERAR O GRAFICO NO FINAL.
    private PainelGantt finalPainelGantt;

    private final CenarioSimulacao cenario;
    private final AlgoritmoEscalonamento algoritmo;
    private Simulador simulador;

    public TelaSimulacao(int numeroMedicos, CenarioSimulacao cenario, AlgoritmoEscalonamento algoritmo) {
        this.cenario = cenario;
        this.algoritmo = algoritmo;

        setTitle("Simulação em Andamento");
        setSize(1800, 1200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        painelGantt = new PainelGantt();
        scrollGantt = new JScrollPane(painelGantt);
        scrollGantt.setBorder(BorderFactory.createTitledBorder("Gráfico de Gantt (Evolução dos Pacientes)"));
        scrollGantt.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollGantt.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollGantt, BorderLayout.CENTER);

        // --- Painel Leste (Info + Resultados) ---
        JPanel painelLeste = new JPanel(new BorderLayout(5, 5));
        painelLeste.setPreferredSize(new Dimension(500, 0));
        painelLeste.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        JPanel painelInfo = new JPanel();
        painelInfo.setLayout(new BoxLayout(painelInfo, BoxLayout.Y_AXIS));

        // --- Status Atual, Médicos, Fila de Prontos ---
        labelTempoAtual = new JLabel("Tempo: 0");
        labelTempoAtual.setFont(new Font("Arial", Font.BOLD, 16));
        JPanel painelTempo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTempo.setBorder(BorderFactory.createTitledBorder("Status Atual"));
        painelTempo.add(labelTempoAtual);
        painelInfo.add(painelTempo);

        JPanel painelContexto = new JPanel(new GridLayout(5, 1, 0, 5));
        painelContexto.setBorder(BorderFactory.createTitledBorder("Contexto da Simulação"));

        // Pega o nome amigável do Enum
        JLabel labelCenario = new JLabel("Cenário: " + this.cenario.getNome());
        JLabel labelAlgoritmo = new JLabel("Algoritmo: " + this.algoritmo.toString());

        painelContexto.add(labelCenario);
        painelContexto.add(labelAlgoritmo);
        painelInfo.add(painelContexto);

        painelInfo.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel painelMedicos = new JPanel(new GridLayout(numeroMedicos, 1, 5, 5));
        painelMedicos.setBorder(BorderFactory.createTitledBorder("Médicos (Núcleos)"));
        labelsStatusMedicos = new ArrayList<>();
        for (int i = 0; i < numeroMedicos; i++) {
            JLabel label = new JLabel("Médico " + (i + 1) + ": OCIOSO");
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            labelsStatusMedicos.add(label);
            painelMedicos.add(label);
        }
        JScrollPane scrollMedicos = new JScrollPane(painelMedicos);
        scrollMedicos.setPreferredSize(new Dimension(0, 100));
        painelInfo.add(scrollMedicos);
        painelInfo.add(Box.createRigidArea(new Dimension(0, 5)));

        areaFilaProntos = new JTextArea("Fila de Prontos: [vazia]");
        areaFilaProntos.setEditable(false);
        areaFilaProntos.setLineWrap(true);
        areaFilaProntos.setWrapStyleWord(true);
        JScrollPane scrollFila = new JScrollPane(areaFilaProntos);
        scrollFila.setBorder(BorderFactory.createTitledBorder("Fila de Prontos"));
        scrollFila.setPreferredSize(new Dimension(0, 100));
        painelInfo.add(scrollFila);

        // --- Painel de Adição Runtime ---
        if (cenario != CenarioSimulacao.CUSTOMIZADO) {
            painelInfo.add(Box.createRigidArea(new Dimension(0, 5)));
            painelInfo.add(criarPainelAdicaoRuntime());
        }

        areaResultados = new JTextArea("Resultados Finais:\n(Aguardando conclusão...)");
        areaResultados.setEditable(false);
        areaResultados.setLineWrap(true);
        areaResultados.setWrapStyleWord(true);
        JScrollPane scrollResultados = new JScrollPane(areaResultados);
        scrollResultados.setBorder(BorderFactory.createTitledBorder("Resultados Finais"));

        painelLeste.add(painelInfo, BorderLayout.NORTH);
        painelLeste.add(scrollResultados, BorderLayout.CENTER);

        add(painelLeste, BorderLayout.EAST);
    }

    /**
     * Cria o painel com botões para adicionar pacientes em runtime.
     */
    private JPanel criarPainelAdicaoRuntime() {
        JPanel painel = new JPanel(new GridLayout(2, 1, 5, 5));
        painel.setBorder(BorderFactory.createTitledBorder("Adicionar Pacientes (Runtime)"));

        JButton btnAddAleatorio = new JButton("Adicionar Paciente Aleatório");
        JButton btnAddManual = new JButton("Adicionar Paciente Manual");

        btnAddAleatorio.addActionListener(e -> adicionarPacienteAleatorio());
        btnAddManual.addActionListener(e -> adicionarPacienteManual());

        painel.add(btnAddAleatorio);
        painel.add(btnAddManual);
        return painel;
    }

    /**
     * Lógica do botão de adição aleatória.
     */
    private void adicionarPacienteAleatorio() {
        if (this.simulador == null) return;

        // Recebe do Gerador um paciente com o perfil do cenário
        Paciente p = GeradorPacientes.gerarPacienteRuntime(this.cenario);

        // Envia para o Simulador (que é thread-safe)
        this.simulador.adicionarPacienteRuntime(p);
    }

    /**
     * Lógica do botão de adição manual.
     */
    private void adicionarPacienteManual() {
        if (this.simulador == null) return;

        // 1. Cria o pop-up (similar ao da TelaConfiguracao)
        JSpinner spinnerDuracao = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        JSpinner spinnerPrioridade = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));

        JPanel painelPopup = new JPanel(new GridLayout(0, 2, 5, 5));
        painelPopup.add(new JLabel("Duração (Burst Time):"));
        painelPopup.add(spinnerDuracao);
        painelPopup.add(new JLabel("Prioridade (1=Alta):"));
        painelPopup.add(spinnerPrioridade);

        int resultado = JOptionPane.showConfirmDialog(this, painelPopup,
                "Adicionar Novo Paciente (Runtime)", JOptionPane.OK_CANCEL_OPTION);

        if (resultado == JOptionPane.OK_OPTION) {
            int duracao = (Integer) spinnerDuracao.getValue();
            int prioridade = (Integer) spinnerPrioridade.getValue();

            // ID e TempoChegada (0) são placeholders
            Paciente p = new Paciente(0, 0, duracao, prioridade);

            // Envia para o Simulador
            this.simulador.adicionarPacienteRuntime(p);
        }
    }

    @Override
    public void atualizarVisualizacao(int tempoAtual, List<Medico> medicos, Queue<Paciente> filaDeProntos) {
        SwingUtilities.invokeLater(() -> {
            labelTempoAtual.setText("Tempo: " + tempoAtual);

            if (filaDeProntos.isEmpty()) {
                areaFilaProntos.setText("[vazia]");
            } else {
                String fila = filaDeProntos.stream()
                        .map(p -> "P" + p.getId())
                        .collect(Collectors.joining(", "));
                areaFilaProntos.setText("[" + fila + "]");
            }

            for (int i = 0; i < medicos.size(); i++) {
                Medico medico = medicos.get(i);
                JLabel label = labelsStatusMedicos.get(i);
                String status;
                if (medico.estaOcioso()) {
                    status = "OCIOSO";
                } else {
                    Paciente p = medico.getPacienteAtual();
                    status = "OCUPADO (Atendendo P" + p.getId() + " | Resta: ".concat(String.valueOf(p.getTempoRestante())).concat(")");
                }
                label.setText("Médico " + medico.getId() + ": " + status);
            }

            painelGantt.registrarTick(tempoAtual, medicos, filaDeProntos);
            scrollGantt.revalidate();
        });
    }

    @Override
    public void exibirMetricasFinais(String resultados) {
        SwingUtilities.invokeLater(() -> {
            areaResultados.setText(resultados);
            setTitle("Simulação Concluída");
            scrollGantt.getHorizontalScrollBar().setValue(0);


            this.finalPainelGantt = painelGantt; // Salva a instância final do Gantt
            exibirGraficoFinalPopup(resultados); // Chama o novo método
        });
    }

    //  Cria e exibe o pop-up com o gráfico e resultados
    private void exibirGraficoFinalPopup(String resultadosFinais) {
        JDialog popup = new JDialog(this, "Resultados Finais da Simulação", Dialog.ModalityType.APPLICATION_MODAL);
        popup.setSize(1000, 700);
        popup.setLocationRelativeTo(this);
        popup.setLayout(new BorderLayout(10, 10));

        // --- Painel para o Gráfico de Gantt no Pop-up --
        JScrollPane scrollPopupGantt = new JScrollPane(finalPainelGantt);
        scrollPopupGantt.setBorder(BorderFactory.createTitledBorder("Gráfico de Gantt Completo"));
        scrollPopupGantt.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPopupGantt.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        popup.add(scrollPopupGantt, BorderLayout.CENTER);

        // --- Painel para os Resultados Finais no Pop-up
        JTextArea areaPopupResultados = new JTextArea(resultadosFinais);
        areaPopupResultados.setEditable(false);
        areaPopupResultados.setLineWrap(true);
        areaPopupResultados.setWrapStyleWord(true);
        areaPopupResultados.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        areaPopupResultados.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Fonte para resultados

        JScrollPane scrollPopupResultados = new JScrollPane(areaPopupResultados);
        scrollPopupResultados.setBorder(BorderFactory.createTitledBorder("Métricas de Desempenho"));
        scrollPopupResultados.setPreferredSize(new Dimension(0, 150)); // Altura fixa
        popup.add(scrollPopupResultados, BorderLayout.SOUTH);

        popup.setVisible(true); // Exibe o pop-up
    }
}
