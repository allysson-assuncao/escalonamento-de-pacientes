package org.example.escalonamentodepacientes.gui;

import org.example.escalonamentodepacientes.escalonadores.IAtualizadorVisual;
import org.example.escalonamentodepacientes.model.Medico;
import org.example.escalonamentodepacientes.model.Paciente;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class TelaSimulacao extends JFrame implements IAtualizadorVisual {

    private JLabel labelTempoAtual;
    private JTextArea areaFilaProntos;
    private JTextArea areaResultados;
    private List<JLabel> labelsStatusMedicos;
    private PainelGantt painelGantt;
    private JScrollPane scrollGantt;

    // Uma cópia do PainelGantt para ser exibida no pop-up final. GERAR O GRAFICO NO FINAL.
    private PainelGantt finalPainelGantt;

    public TelaSimulacao(int numeroMedicos) {
        setTitle("Simulação em Andamento");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        painelGantt = new PainelGantt();
        scrollGantt = new JScrollPane(painelGantt);
        scrollGantt.setBorder(BorderFactory.createTitledBorder("Gráfico de Gantt (Evolução dos Pacientes)"));
        scrollGantt.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollGantt.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollGantt, BorderLayout.CENTER);

        JPanel painelLeste = new JPanel(new BorderLayout(5, 5));
        painelLeste.setPreferredSize(new Dimension(350, 0));
        painelLeste.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        JPanel painelInfo = new JPanel();
        painelInfo.setLayout(new BoxLayout(painelInfo, BoxLayout.Y_AXIS));

        labelTempoAtual = new JLabel("Tempo: 0");
        labelTempoAtual.setFont(new Font("Arial", Font.BOLD, 16));
        JPanel painelTempo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTempo.setBorder(BorderFactory.createTitledBorder("Status Atual"));
        painelTempo.add(labelTempoAtual);
        painelInfo.add(painelTempo);
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