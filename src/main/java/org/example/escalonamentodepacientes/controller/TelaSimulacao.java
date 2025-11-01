package org.example.escalonamentodepacientes.controller;

import org.example.escalonamentodepacientes.enums.AlgoritmoEscalonamento;
import org.example.escalonamentodepacientes.model.ConfiguracaoSimulacao;
import org.example.escalonamentodepacientes.model.Paciente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Tela principal para configuração da simulação.
 * Coleta os dados de entrada e inicia o Simulador.
 */
public class TelaSimulacao extends JFrame {

    private JComboBox<AlgoritmoEscalonamento> comboAlgoritmo;
    private JComboBox<String> comboCargaPacientes;
    private JSpinner spinnerMedicos;
    private JSpinner spinnerQuantum;
    private JSpinner spinnerQtdPacientesAleatorios;
    private JRadioButton radioAleatorio;
    private JRadioButton radioPredefinido;

    public TelaSimulacao() {
        setTitle("Simulador de Hospital Digital - Configuração");
        setSize(500, 350);
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
        spinnerQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        add(spinnerQuantum);

        // 4. Seleção de Pacientes (Aleatório)
        radioAleatorio = new JRadioButton("Pacientes Aleatórios:", true);
        spinnerQtdPacientesAleatorios = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        add(radioAleatorio);
        add(spinnerQtdPacientesAleatorios);

        // 5. Seleção de Pacientes (Pré-definido)
        radioPredefinido = new JRadioButton("Carga Pré-definida:");
        comboCargaPacientes = new JComboBox<>(new String[]{"CenarioSJF", "CenarioPrioridade", "CenarioSRTF"});
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
        if(algoritmo == AlgoritmoEscalonamento.ROUND_ROBIN){
            ConfiguracaoSimulacao config = new ConfiguracaoSimulacao(algoritmo, numMedicos, pacientes, quantum);
        }else{
            ConfiguracaoSimulacao config = new ConfiguracaoSimulacao(algoritmo, numMedicos, pacientes);
        }

        System.out.println("Iniciando simulação com " + numMedicos + " médicos e algoritmo " + algoritmo);
        System.out.println("Pacientes:");
        pacientes.forEach(System.out::println);



    }

}
