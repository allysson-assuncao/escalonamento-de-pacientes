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

/**
 * Tela que exibe a simulação ocorrendo dinamicamente.
 * Implementa IAtualizadorVisual para receber callbacks do Simulador.
 */
public class TelaSimulacao extends JFrame implements IAtualizadorVisual {

    private JLabel labelTempoAtual;
    private JTextArea areaFilaProntos;
    private JTextArea areaResultados;
    private List<JLabel> labelsStatusMedicos; // Labels para status de cada médico

    public TelaSimulacao(int numeroMedicos) {
        setTitle("Simulação em Andamento");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Painel Superior (Tempo e Fila) ---
        JPanel painelTopo = new JPanel(new BorderLayout());
        labelTempoAtual = new JLabel("Tempo: 0");
        labelTempoAtual.setFont(new Font("Arial", Font.BOLD, 16));
        painelTopo.add(labelTempoAtual, BorderLayout.NORTH);

        areaFilaProntos = new JTextArea("Fila de Prontos: [vazia]");
        areaFilaProntos.setEditable(false);
        areaFilaProntos.setLineWrap(true);
        painelTopo.add(new JScrollPane(areaFilaProntos), BorderLayout.CENTER);

        add(painelTopo, BorderLayout.NORTH);

        // --- Painel Central (Médicos) ---
        JPanel painelMedicos = new JPanel(new GridLayout(numeroMedicos, 1));
        painelMedicos.setBorder(BorderFactory.createTitledBorder("Médicos (Núcleos)"));
        labelsStatusMedicos = new ArrayList<>();
        for (int i = 0; i < numeroMedicos; i++) {
            JLabel label = new JLabel("Médico " + (i + 1) + ": OCIOSO");
            labelsStatusMedicos.add(label);
            painelMedicos.add(label);
        }
        add(new JScrollPane(painelMedicos), BorderLayout.CENTER);

        // --- Painel Inferior (Resultados) ---
        areaResultados = new JTextArea("Resultados Finais:\n(Aguardando conclusão...)");
        areaResultados.setEditable(false);
        add(new JScrollPane(areaResultados), BorderLayout.SOUTH);
    }

    @Override
    public void atualizarVisualizacao(int tempoAtual, List<Medico> medicos, Queue<Paciente> filaDeProntos) {
        // Esta atualização vem de outra thread (SwingWorker)
        // Usamos SwingUtilities.invokeLater para garantir a segurança da thread
        SwingUtilities.invokeLater(() -> {
            // 1. Atualiza o Tempo
            labelTempoAtual.setText("Tempo: " + tempoAtual);

            // 2. Atualiza a Fila de Prontos
            if (filaDeProntos.isEmpty()) {
                areaFilaProntos.setText("Fila de Prontos: [vazia]");
            } else {
                String fila = filaDeProntos.stream()
                        .map(p -> "P" + p.getId())
                        .collect(Collectors.joining(", "));
                areaFilaProntos.setText("Fila de Prontos: [" + fila + "]");
            }

            // 3. Atualiza os Médicos
            for (int i = 0; i < medicos.size(); i++) {
                Medico medico = medicos.get(i);
                JLabel label = labelsStatusMedicos.get(i);
                String status;
                if (medico.estaOcioso()) {
                    status = "OCIOSO";
                } else {
                    Paciente p = medico.getPacienteAtual();
                    status = "OCUPADO (Atendendo P" + p.getId() + " | Resta: " + p.getTempoRestante() + ")";
                }
                label.setText("Médico " + medico.getId() + ": " + status);
            }
        });
    }

    @Override
    public void exibirMetricasFinais(String resultados) {
        SwingUtilities.invokeLater(() -> {
            areaResultados.setText(resultados);
            setTitle("Simulação Concluída");
        });
    }
}
