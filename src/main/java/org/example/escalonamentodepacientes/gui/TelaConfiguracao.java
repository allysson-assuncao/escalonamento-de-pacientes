package org.example.escalonamentodepacientes.gui;

import org.example.escalonamentodepacientes.enums.AlgoritmoEscalonamento;
import org.example.escalonamentodepacientes.model.ConfiguracaoSimulacao;
import org.example.escalonamentodepacientes.model.Paciente;
import org.example.escalonamentodepacientes.simulacao.Simulador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
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
    private JRadioButton radioManual;
    private DefaultListModel<Paciente> modeloListaPacientes;
    private JList<Paciente> listaManualPacientes;
    private JButton btnAddPaciente;
    private JButton btnRemovePaciente;
    private JButton btnSubir;
    private JButton btnDescer;
    private JPanel painelListaManual; // Painel que agrupa os controles da lista
    private int proximoIdPacienteManual = 1;

    public TelaConfiguracao() {
        setTitle("Simulador de Hospital Digital - Configuração");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // Usamos um layout mais flexível para agrupar os painéis
        setLayout(new BorderLayout(10, 10));

        // --- Painel de Configurações (Tudo exceto a lista) ---
        JPanel painelConfig = new JPanel();
        // GridLayout flexível (N linhas, 2 colunas)
        painelConfig.setLayout(new GridLayout(0, 2, 10, 10));
        painelConfig.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Algoritmo
        painelConfig.add(new JLabel("Algoritmo:"));
        comboAlgoritmo = new JComboBox<>(AlgoritmoEscalonamento.values());
        painelConfig.add(comboAlgoritmo);

        // 2. Número de Médicos
        painelConfig.add(new JLabel("Nº de Médicos (1, 2 ou 4):"));
        spinnerMedicos = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        painelConfig.add(spinnerMedicos);

        // 3. Quantum (para RR)
        painelConfig.add(new JLabel("Quantum (para Round-Robin):"));
        spinnerQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        painelConfig.add(spinnerQuantum);

        // Agrupando os RadioButtons
        ButtonGroup grupoPacientes = new ButtonGroup();

        // 4. Seleção de Pacientes (Aleatório)
        radioAleatorio = new JRadioButton("Pacientes Aleatórios:", true);
        spinnerQtdPacientesAleatorios = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        grupoPacientes.add(radioAleatorio);
        painelConfig.add(radioAleatorio);
        painelConfig.add(spinnerQtdPacientesAleatorios);

        // 5. Seleção de Pacientes (Pré-definido)
        radioPredefinido = new JRadioButton("Carga Pré-definida:");
        comboCargaPacientes = new JComboBox<>(new String[]{"CenarioSJF", "CenarioPrioridade", "CenarioSRTF"});
        grupoPacientes.add(radioPredefinido);
        painelConfig.add(radioPredefinido);
        painelConfig.add(comboCargaPacientes);

        // 6. Seleção de Pacientes (Manual)
        radioManual = new JRadioButton("Adicionar Manualmente:");
        grupoPacientes.add(radioManual);
        painelConfig.add(radioManual);
        // Adicionamos um painel vazio como placeholder,
        // o painel da lista será adicionado no final
        painelConfig.add(new JLabel(""));

        add(painelConfig, BorderLayout.NORTH);

        // --- Painel de Gerenciamento da Lista Manual ---
        painelListaManual = new JPanel(new BorderLayout(5, 5));
        painelListaManual.setBorder(BorderFactory.createTitledBorder("Pacientes Adicionados Manualmente"));

        modeloListaPacientes = new DefaultListModel<>();
        listaManualPacientes = new JList<>(modeloListaPacientes);
        listaManualPacientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // O Paciente.toString() é usado para exibir na lista
        painelListaManual.add(new JScrollPane(listaManualPacientes), BorderLayout.CENTER);

        // Painel de botões da lista
        JPanel painelBotoesLista = new JPanel(new FlowLayout());
        btnAddPaciente = new JButton("Adicionar");
        btnRemovePaciente = new JButton("Remover");
        btnSubir = new JButton("Subir");
        btnDescer = new JButton("Descer");

        // Adiciona os listeners
        btnAddPaciente.addActionListener(this::addPaciente);
        btnRemovePaciente.addActionListener(this::removePaciente);
        btnSubir.addActionListener(this::subirPaciente);
        btnDescer.addActionListener(this::descerPaciente);

        painelBotoesLista.add(btnAddPaciente);
        painelBotoesLista.add(btnRemovePaciente);
        painelBotoesLista.add(btnSubir);
        painelBotoesLista.add(btnDescer);

        painelListaManual.add(painelBotoesLista, BorderLayout.SOUTH);

        // Adiciona o painel da lista ao centro da janela principal
        add(painelListaManual, BorderLayout.CENTER);

        // --- Botão Iniciar (no final) ---
        JButton btnIniciar = new JButton("Iniciar Simulação");
        btnIniciar.addActionListener(this::iniciarSimulacao);
        add(btnIniciar, BorderLayout.SOUTH);

        // --- Listeners dos RadioButtons para habilitar/desabilitar ---
        radioAleatorio.addActionListener(this::atualizarEstadoInputs);
        radioPredefinido.addActionListener(this::atualizarEstadoInputs);
        radioManual.addActionListener(this::atualizarEstadoInputs);

        // Define o estado inicial correto
        atualizarEstadoInputs(null);
    }

    /**
     * Habilita e desabilita os campos de entrada de acordo com o
     * RadioButton selecionado.
     */
    private void atualizarEstadoInputs(ActionEvent e) {
        boolean aleatorio = radioAleatorio.isSelected();
        boolean predefinido = radioPredefinido.isSelected();
        boolean manual = radioManual.isSelected();

        // Campos de Aleatório
        spinnerQtdPacientesAleatorios.setEnabled(aleatorio);

        // Campos Pré-definidos
        comboCargaPacientes.setEnabled(predefinido);

        // Campos Manuais (o painel inteiro)
        painelListaManual.setEnabled(manual);
        listaManualPacientes.setEnabled(manual);
        btnAddPaciente.setEnabled(manual);
        btnRemovePaciente.setEnabled(manual);
        btnSubir.setEnabled(manual);
        btnDescer.setEnabled(manual);
    }

    /**
     * Abre um pop-up para adicionar um novo paciente à lista manual.
     */
    private void addPaciente(ActionEvent e) {
        // 1. Cria os componentes do pop-up
        JSpinner spinnerChegada = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JSpinner spinnerDuracao = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        JSpinner spinnerPrioridade = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));

        // 2. Cria um painel para organizar os componentes
        JPanel painelPopup = new JPanel(new GridLayout(0, 2, 5, 5));
        painelPopup.add(new JLabel("Tempo de Chegada:"));
        painelPopup.add(spinnerChegada);
        painelPopup.add(new JLabel("Duração (Burst Time):"));
        painelPopup.add(spinnerDuracao);
        painelPopup.add(new JLabel("Prioridade (1=Alta):"));
        painelPopup.add(spinnerPrioridade);

        // 3. Exibe o pop-up (JOptionPane customizado)
        int resultado = JOptionPane.showConfirmDialog(this, painelPopup,
                "Adicionar Novo Paciente", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        // 4. Processa o resultado
        if (resultado == JOptionPane.OK_OPTION) {
            int chegada = (Integer) spinnerChegada.getValue();
            int duracao = (Integer) spinnerDuracao.getValue();
            int prioridade = (Integer) spinnerPrioridade.getValue();

            // Cria o paciente com um ID único e o adiciona ao modelo da lista
            Paciente novoPaciente = new Paciente(
                this.proximoIdPacienteManual++,
                chegada,
                duracao,
                prioridade
            );
            modeloListaPacientes.addElement(novoPaciente);
        }
    }

    /**
     * Remove o paciente selecionado da lista manual.
     */
    private void removePaciente(ActionEvent e) {
        int indiceSelecionado = listaManualPacientes.getSelectedIndex();
        if (indiceSelecionado != -1) { // Verifica se algo está selecionado
            modeloListaPacientes.remove(indiceSelecionado);
        } else {
            JOptionPane.showMessageDialog(this,
                "Selecione um paciente na lista para remover.",
                "Erro", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Move o paciente selecionado uma posição para cima na lista.
     */
    private void subirPaciente(ActionEvent e) {
        int indice = listaManualPacientes.getSelectedIndex();
        // Verifica se é possível mover (não é o primeiro)
        if (indice > 0) {
            // Remove o elemento e o re-insere na posição anterior
            Paciente p = modeloListaPacientes.remove(indice);
            modeloListaPacientes.add(indice - 1, p);
            // Mantém o item movido selecionado
            listaManualPacientes.setSelectedIndex(indice - 1);
        }
    }

    /**
     * Move o paciente selecionado uma posição para baixo na lista.
     */
    private void descerPaciente(ActionEvent e) {
        int indice = listaManualPacientes.getSelectedIndex();
        // Verifica se é possível mover (não é o último)
        if (indice != -1 && indice < modeloListaPacientes.getSize() - 1) {
            // Remove o elemento e o re-insere na posição seguinte
            Paciente p = modeloListaPacientes.remove(indice);
            modeloListaPacientes.add(indice + 1, p);
            // Mantém o item movido selecionado
            listaManualPacientes.setSelectedIndex(indice + 1);
        }
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

        } else if (radioPredefinido.isSelected()) {
            String cenario = (String) comboCargaPacientes.getSelectedItem();
            pacientes = GeradorPacientes.getListaPredefinida(cenario);

        } else if (radioManual.isSelected()) {
            if (modeloListaPacientes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Adicione pelo menos um paciente na lista manual para iniciar.",
                    "Lista Vazia", JOptionPane.WARNING_MESSAGE);
                return; // Para a execução
            }
            // Converte o DefaultListModel para uma List
            pacientes = Collections.list(modeloListaPacientes.elements());

        } else {
             // Caso de emergência (nenhum selecionado)
            JOptionPane.showMessageDialog(this, "Selecione uma fonte de pacientes.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
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
