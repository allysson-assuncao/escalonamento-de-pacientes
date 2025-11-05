package org.example.escalonamentodepacientes.gui;

import org.example.escalonamentodepacientes.enums.AlgoritmoEscalonamento;
import org.example.escalonamentodepacientes.model.ConfiguracaoSimulacao;
import org.example.escalonamentodepacientes.model.Paciente;
import org.example.escalonamentodepacientes.simulacao.Simulador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Tela principal para configuração da simulação.
 * Coleta os dados de entrada e inicia o Simulador.
 */
public class TelaConfiguracao extends JFrame {

    private JComboBox<AlgoritmoEscalonamento> comboAlgoritmo;
    private JComboBox<String> comboCargaPacientes;
    private JSpinner spinnerMedicos;
    private JSpinner spinnerQuantum;
    private JSpinner spinnerQtdPacientesAleatorios;
    private JRadioButton radioAleatorio;
    private JRadioButton radioPredefinido;

    public TelaConfiguracao() {
        setTitle("Simulador de Hospital Digital - Configuração");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2, 10, 10));

        // 1. Algoritmo
        add(new JLabel("Algoritmo:"));
        comboAlgoritmo = new JComboBox<>(AlgoritmoEscalonamento.values());
        add(comboAlgoritmo);

        // 2. Número de Médicos
        add(new JLabel("Nº de Médicos (1, 2 ou 4):"));
        spinnerMedicos = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        add(spinnerMedicos);

        // 3. Quantum (para RR)
        add(new JLabel("Quantum (para Round-Robin):"));
        spinnerQuantum = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        add(spinnerQuantum);

        // 4. Seleção de Pacientes (Aleatório)
        radioAleatorio = new JRadioButton("Pacientes Aleatórios:", true);
        spinnerQtdPacientesAleatorios = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        add(radioAleatorio);
        add(spinnerQtdPacientesAleatorios);

        // 5. Seleção de Pacientes (Pré-definido)
        radioPredefinido = new JRadioButton("Carga Pré-definida:");
        comboCargaPacientes = new JComboBox<>(new String[]{"CenarioSlides", "CenarioSJF", "CenarioPrioridade", "CenarioSRTF"});
        add(radioPredefinido);
        add(comboCargaPacientes);

        // Agrupando os RadioButtons
        ButtonGroup grupoPacientes = new ButtonGroup();
        grupoPacientes.add(radioAleatorio);
        grupoPacientes.add(radioPredefinido);

        // 6. Botão Iniciar
        add(new JLabel("")); // Placeholder
        JButton btnIniciar = new JButton("Iniciar Simulação");
        btnIniciar.addActionListener(this::iniciarSimulacao);
        add(btnIniciar);
    }

    /**
     * Ação do botão "Iniciar Simulação".
     * Coleta os dados, cria a Configuração e chama o Simulador.
     */
    private void iniciarSimulacao(ActionEvent e) {
        // 1. Coletar dados da GUI
        AlgoritmoEscalonamento algoritmo = (AlgoritmoEscalonamento) comboAlgoritmo.getSelectedItem();
        int numMedicos = (Integer) spinnerMedicos.getValue();
        int quantum = (Integer) spinnerQuantum.getValue();

        List<Paciente> pacientes;
        if (radioAleatorio.isSelected()) {
            int qtd = (Integer) spinnerQtdPacientesAleatorios.getValue();
            pacientes = GeradorPacientes.gerarListaAleatoria(qtd);
        } else {
            String cenario = (String) comboCargaPacientes.getSelectedItem();
            pacientes = GeradorPacientes.getListaPredefinida(cenario);
        }

        // 2. Criar Configuração
        ConfiguracaoSimulacao config = new ConfiguracaoSimulacao(algoritmo, numMedicos, pacientes, quantum);

        // 3. Criar e exibir a tela de Simulação
        TelaSimulacao telaSimulacao = new TelaSimulacao(numMedicos);
        telaSimulacao.setVisible(true);

        // 4. Instanciar o simulador, passando a nova tela
        Simulador simulador = new Simulador(config, telaSimulacao);

        // 5. Executa o simulador em background (swingworker)
        // Evita o travamento da GUI
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                simulador.executarSimulacao();
                return null;
            }
        };

        worker.execute();

        // Desabilita a tela enquanto um teste já está em execução
        this.setVisible(false);
    }

}
