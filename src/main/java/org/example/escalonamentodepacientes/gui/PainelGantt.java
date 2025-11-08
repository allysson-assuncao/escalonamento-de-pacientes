package org.example.escalonamentodepacientes.gui;

import org.example.escalonamentodepacientes.model.Medico;
import org.example.escalonamentodepacientes.model.Paciente;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PainelGantt extends JPanel {

    private static final int ALTURA_BARRA = 25;
    private static final int MARGEM_SUPERIOR = 30;
    private static final int MARGEM_ESQUERDA = 100;
    private static final int LARGURA_TICK = 25;

    // Paciente -> cores por tick (apenas após chegada)
    private final Map<Paciente, List<Color>> historicoPacientes = new LinkedHashMap<>();

    // Paciente -> tick em que entrou pela primeira vez (momento de chegada)
    private final Map<Paciente, Integer> tempoChegada = new HashMap<>();

    // Médico -> cor fixa
    private final Map<Medico, Color> coresMedicos = new HashMap<>();

    private int tempoAtual = 0;

    public PainelGantt() {
        setBackground(Color.WHITE);
    }

    public void registrarTick(int tempo, List<Medico> medicos, Queue<Paciente> filaDeProntos) {
        this.tempoAtual = tempo;

        // Pacientes envolvidos neste tick (em execução ou prontos)
        Set<Paciente> pacientesAtivos = new LinkedHashSet<>(filaDeProntos);
        for (Medico m : medicos) {
            if (m.getPacienteAtual() != null) {
                pacientesAtivos.add(m.getPacienteAtual());
            }
        }

        // Atualiza ou cria histórico apenas se o paciente já entrou no sistema
        for (Paciente p : pacientesAtivos) {
            historicoPacientes.putIfAbsent(p, new ArrayList<>());
            tempoChegada.putIfAbsent(p, tempo);

            // Define cor atual (branco = espera/pronto)
            Color cor = Color.WHITE;

            // Se estiver em execução, cor do médico
            for (Medico m : medicos) {
                if (m.getPacienteAtual() == p) {
                    coresMedicos.putIfAbsent(m, gerarCorAleatoria(m.getId()));
                    cor = coresMedicos.get(m);
                    break;
                }
            }

            // Preenche ticks anteriores (se perdeu algum)
            List<Color> cores = historicoPacientes.get(p);
            while (cores.size() < tempo - tempoChegada.get(p)) {
                cores.add(Color.WHITE);
            }

            cores.add(cor);
        }

        repaint();
    }

    private Color gerarCorAleatoria(int idMedico) {
        switch (idMedico) {
            case 1: return new Color(255, 99, 71);     // Vermelho
            case 2: return new Color(65, 105, 225);    // Azul
            case 3: return new Color(60, 179, 113);    // Verde
            case 4: return new Color(255, 215, 0);     // Amarelo
            default: return Color.GRAY;                         // Cor padrão
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = MARGEM_SUPERIOR;

        // Desenha cada paciente (em ordem de chegada)
        List<Map.Entry<Paciente, Integer>> pacientesOrdenados = new ArrayList<>(tempoChegada.entrySet());
        pacientesOrdenados.sort(Map.Entry.comparingByValue());

        for (Map.Entry<Paciente, Integer> entry : pacientesOrdenados) {
            Paciente p = entry.getKey();
            int inicio = entry.getValue();
            List<Color> cores = historicoPacientes.get(p);
            if (cores == null) continue;

            // Nome do paciente no eixo Y
            g2.setColor(Color.BLACK);
            g2.drawString("P" + p.getId(), 20, y + ALTURA_BARRA - 5);

            // Barra (a partir do tempo de chegada)
            int x = MARGEM_ESQUERDA + inicio * LARGURA_TICK;
            for (Color cor : cores) {
                g2.setColor(cor);
                g2.fillRect(x, y, LARGURA_TICK, ALTURA_BARRA - 2);
                g2.setColor(Color.GRAY);
                g2.drawRect(x, y, LARGURA_TICK, ALTURA_BARRA - 2);
                x += LARGURA_TICK;
            }

            y += ALTURA_BARRA + 5;
        }

        // Eixo X (ticks)
        g2.setColor(Color.BLACK);
        g2.drawLine(MARGEM_ESQUERDA, MARGEM_SUPERIOR - 10, MARGEM_ESQUERDA + tempoAtual * LARGURA_TICK, MARGEM_SUPERIOR - 10);

        for (int t = 0; t <= tempoAtual; t += 5) {
            int x = MARGEM_ESQUERDA + t * LARGURA_TICK;
            g2.drawLine(x, MARGEM_SUPERIOR - 15, x, MARGEM_SUPERIOR - 5);
            g2.drawString(String.valueOf(t), x - 5, MARGEM_SUPERIOR - 20);
        }

        // 🔹 Legenda dos médicos
        if (!coresMedicos.isEmpty()) {
            int legendaY = y + 40;
            int legendaX = MARGEM_ESQUERDA;
            g2.setColor(Color.BLACK);
            g2.drawString("Legenda:", legendaX, legendaY);
            legendaX += 70;
            for (Map.Entry<Medico, Color> entry : coresMedicos.entrySet()) {
                Medico medico = entry.getKey();
                Color cor = entry.getValue();
                g2.setColor(cor);
                g2.fillRect(legendaX, legendaY - 15, 20, 15);
                g2.setColor(Color.GRAY);
                g2.drawRect(legendaX, legendaY - 15, 20, 15);
                g2.setColor(Color.BLACK);
                g2.drawString("Médico " + medico.getId(), legendaX + 30, legendaY - 3);
                legendaX += 120;
            }
            y += 60;
        }

        // Ajuste automático do tamanho
        setPreferredSize(new Dimension(MARGEM_ESQUERDA + tempoAtual * LARGURA_TICK + 200, y + 100));

    }
}
