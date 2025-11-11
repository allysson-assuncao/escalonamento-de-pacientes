package org.example.escalonamentodepacientes.gui;

import org.example.escalonamentodepacientes.enums.AlgoritmoEscalonamento;
import org.example.escalonamentodepacientes.enums.CenarioSimulacao;
import org.example.escalonamentodepacientes.model.ConfiguracaoSimulacao;
import org.example.escalonamentodepacientes.model.Paciente;
import org.example.escalonamentodepacientes.simulacao.GeradorPacientes;
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

    // --- Componentes originais ---
    private JComboBox<AlgoritmoEscalonamento> comboAlgoritmo;
    private JSpinner spinnerMedicos;
    private JSpinner spinnerQuantum;

    // --- Componentes da Fonte de Pacientes ---
    private JPanel painelFontePacientes;
    private JRadioButton radioAleatorio;
    private JRadioButton radioPredefinido;
    private JRadioButton radioManual;
    private JSpinner spinnerQtdPacientesAleatorios;
    private JComboBox<String> comboCargaPacientes;
    private ButtonGroup grupoPacientes;

    // --- Componentes da Lista Manual ---
    private JPanel painelListaManual;
    private DefaultListModel<Paciente> modeloListaPacientes;
    private JList<Paciente> listaManualPacientes;
    private JButton btnAddPaciente;
    private JButton btnRemovePaciente;
    private JButton btnSubir;
    private JButton btnDescer;
    private int proximoIdPacienteManual = 1;

    // --- Componentes dos Cenários de Simulação ---
    private JComboBox<CenarioSimulacao> comboCenario;

    public TelaConfiguracao() {
        setTitle("Simulador de Hospital Digital - Configuração");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));


        // 1. Criamos um painel superior que vai conter os nossos "Cards"
        // Usamos um BoxLayout para empilhar os cards verticalmente
        JPanel painelSuperior = new JPanel();
        painelSuperior.setLayout(new BoxLayout(painelSuperior, BoxLayout.Y_AXIS));

        // 2. Criamos e adicionamos o primeiro card (Configurações)
        painelSuperior.add(criarPainelGeral());

        // Adiciona um "espaçador" vertical entre os cards
        painelSuperior.add(Box.createRigidArea(new Dimension(0, 10)));

        // 3. Criamos e adicionamos o segundo card (Fonte dos Pacientes)
        painelSuperior.add(criarPainelListaManual());

        // 4. Adicionamos o painel superior (com os cards)
        add(painelSuperior, BorderLayout.NORTH);


        // --- Painel de Gerenciamento da Lista Manual
        painelListaManual = new JPanel(new BorderLayout(5, 5));
        painelListaManual.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 0, 10),
                BorderFactory.createTitledBorder("Pacientes Adicionados Manualmente")
        ));

        modeloListaPacientes = new DefaultListModel<>();
        listaManualPacientes = new JList<>(modeloListaPacientes);
        listaManualPacientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        painelListaManual.add(new JScrollPane(listaManualPacientes), BorderLayout.CENTER);

        JPanel painelBotoesLista = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        btnAddPaciente = new JButton("Adicionar");
        btnRemovePaciente = new JButton("Remover");
        btnSubir = new JButton("Subir");
        btnDescer = new JButton("Descer");

        btnAddPaciente.addActionListener(this::addPaciente);
        btnRemovePaciente.addActionListener(this::removePaciente);
        btnSubir.addActionListener(this::subirPaciente);
        btnDescer.addActionListener(this::descerPaciente);

        painelBotoesLista.add(btnAddPaciente);
        painelBotoesLista.add(btnRemovePaciente);
        painelBotoesLista.add(btnSubir);
        painelBotoesLista.add(btnDescer);

        painelListaManual.add(painelBotoesLista, BorderLayout.SOUTH);
        add(painelListaManual, BorderLayout.CENTER);

        // Painel Iniciar (Sul)
        add(criarPainelSul(), BorderLayout.SOUTH);

        // Listeners dos RadioButtons
        radioAleatorio.addActionListener(this::atualizarEstadoInputs);
        radioPredefinido.addActionListener(this::atualizarEstadoInputs);
        radioManual.addActionListener(this::atualizarEstadoInputs);

        // --- Listener para o ComboBox de Cenários ---
        comboCenario.addActionListener(this::atualizarEstadoInputs);

        atualizarEstadoInputs(null);
    }


    /**
     * Cria o Card "Configurações da Simulação"
     */
    private JPanel criarPainelGeral() {
        JPanel painel = new JPanel(new GridLayout(0, 2, 10, 10));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10), // Margem externa
                BorderFactory.createTitledBorder("Configurações da Simulação") // Borda com título
        ));

        // 1. Seletor de Cenáris
        painel.add(new JLabel("Cenário de Simulação:"));
        comboCenario = new JComboBox<>(CenarioSimulacao.values());
        painel.add(comboCenario);

        // 2. Algoritmo
        painel.add(new JLabel("Algoritmo:"));
        comboAlgoritmo = new JComboBox<>(AlgoritmoEscalonamento.values());
        painel.add(comboAlgoritmo);

        // 3. Número de Médicos
        painel.add(new JLabel("Nº de Médicos (1, 2 ou 4):"));
        spinnerMedicos = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        painel.add(spinnerMedicos);

        // 4. Quantum (para RR)
        painel.add(new JLabel("Quantum (para Round-Robin):"));
        spinnerQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        painel.add(spinnerQuantum);

        return painel;
    }

    /**
     * Cria o Card "Fonte dos Pacientes" usando um GridBagLayout
     */
    private JPanel criarPainelListaManual() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10), // Margem externa
                BorderFactory.createTitledBorder("Fonte dos Pacientes") // Borda com título
        ));

        modeloListaPacientes = new DefaultListModel<>();
        listaManualPacientes = new JList<>(modeloListaPacientes);
        listaManualPacientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        painel.add(new JScrollPane(listaManualPacientes), BorderLayout.CENTER);

        // GridBagConstraints é o
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaçamento entre componentes
        gbc.anchor = GridBagConstraints.WEST; // Alinhar à esquerda

        // Inicializa os componentes
        grupoPacientes = new ButtonGroup();

        radioAleatorio = new JRadioButton("Pacientes Aleatórios:", true);
        spinnerQtdPacientesAleatorios = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        grupoPacientes.add(radioAleatorio);

        radioPredefinido = new JRadioButton("Carga Pré-definida:");
        comboCargaPacientes = new JComboBox<>(new String[]{"CenarioSlides", "CenarioSJF", "CenarioPrioridade", "CenarioSRTF"});
        grupoPacientes.add(radioPredefinido);

        radioManual = new JRadioButton("Adicionar Manualmente:");
        grupoPacientes.add(radioManual);

        // --- Adicionando os componentes ao painel com o GridBagLayout ---

        // Linha 0: Aleatório
        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(radioAleatorio, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Faz o spinner preencher o espaço
        gbc.weightx = 1.0; // Permite que a coluna 1 estique
        painel.add(spinnerQtdPacientesAleatorios, gbc);

        // Linha 1: Pré-definido
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE; // Reseta o fill
        gbc.weightx = 0; // Reseta o weight
        painel.add(radioPredefinido, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        painel.add(comboCargaPacientes, gbc);

        // Linha 2: Manual
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painel.add(radioManual, gbc);

        return painel;
    }

    /**
     * Cria o Painel Sul com o botão Iniciar (Separado para organização)
     */
    private JPanel criarPainelSul() {
        JButton btnIniciar = new JButton("Iniciar Simulação");
        btnIniciar.setFont(btnIniciar.getFont().deriveFont(Font.BOLD, 14f));
        btnIniciar.addActionListener(this::iniciarSimulacao);

        JPanel painelSul = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelSul.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        painelSul.add(btnIniciar);
        return painelSul;
    }

    /**
     * Habilita e desabilita os campos de entrada (Lógica permanece igual)
     */
    private void atualizarEstadoInputs(ActionEvent e) {
        CenarioSimulacao cenarioSelecionado = (CenarioSimulacao) comboCenario.getSelectedItem();
        boolean customizado = (cenarioSelecionado == CenarioSimulacao.CUSTOMIZADO);

        // 1. Trava/Destrava o Spinner de Médicos
        spinnerMedicos.setEnabled(customizado);
        if (!customizado) {
            assert cenarioSelecionado != null;
            spinnerMedicos.setValue(cenarioSelecionado.getNumeroMedicos());
        }

        // 2. Trava/Destrava o Card "Fonte dos Pacientes"
        painelFontePacientes.setEnabled(customizado);
        for (Component c : painelFontePacientes.getComponents()) {
            c.setEnabled(customizado);
        }
        // ... e seus sub-componentes (radios, spinners)
        radioAleatorio.setEnabled(customizado);
        radioPredefinido.setEnabled(customizado);
        radioManual.setEnabled(customizado);

        // Habilita os spinners/combos internos *somente se*
        // o modo customizado E o radio específico estiverem selecionados
        boolean aleatorio = customizado && radioAleatorio.isSelected();
        boolean predefinido = customizado && radioPredefinido.isSelected();
        boolean manual = customizado && radioManual.isSelected();

        spinnerQtdPacientesAleatorios.setEnabled(aleatorio);
        comboCargaPacientes.setEnabled(predefinido);

        // 3. Trava/Destrava o Painel de Lista Manual
        painelListaManual.setEnabled(manual);
        listaManualPacientes.setEnabled(manual);
        btnAddPaciente.setEnabled(manual);
        btnRemovePaciente.setEnabled(manual);
        btnSubir.setEnabled(manual);
        btnDescer.setEnabled(manual);
        listaManualPacientes.setBackground(manual ? UIManager.getColor("List.background") : UIManager.getColor("Panel.background"));
    }

    /**
     * Abre um pop-up para adicionar um novo paciente (Lógica permanece igual)
     */
    private void addPaciente(ActionEvent e) {
        JSpinner spinnerChegada = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JSpinner spinnerDuracao = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        JSpinner spinnerPrioridade = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));

        JPanel painelPopup = new JPanel(new GridLayout(0, 2, 5, 5));
        painelPopup.add(new JLabel("Tempo de Chegada:"));
        painelPopup.add(spinnerChegada);
        painelPopup.add(new JLabel("Duração (Burst Time):"));
        painelPopup.add(spinnerDuracao);
        painelPopup.add(new JLabel("Prioridade (1=Alta):"));
        painelPopup.add(spinnerPrioridade);

        int resultado = JOptionPane.showConfirmDialog(this, painelPopup,
                "Adicionar Novo Paciente", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            int chegada = (Integer) spinnerChegada.getValue();
            int duracao = (Integer) spinnerDuracao.getValue();
            int prioridade = (Integer) spinnerPrioridade.getValue();

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
     * Remove o paciente selecionado
     */

    private void removePaciente(ActionEvent e) {
        int indiceSelecionado = listaManualPacientes.getSelectedIndex();
        if (indiceSelecionado != -1) {
            modeloListaPacientes.remove(indiceSelecionado);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Selecione um paciente na lista para remover.",
                    "Erro", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Move o paciente selecionado para cima
     */

    private void subirPaciente(ActionEvent e) {
        int indice = listaManualPacientes.getSelectedIndex();
        if (indice > 0) {
            Paciente p = modeloListaPacientes.remove(indice);
            modeloListaPacientes.add(indice - 1, p);
            listaManualPacientes.setSelectedIndex(indice - 1);
        }
    }

    /**
     * Move o paciente selecionado para baixo
     */
    private void descerPaciente(ActionEvent e) {
        int indice = listaManualPacientes.getSelectedIndex();
        if (indice != -1 && indice < modeloListaPacientes.getSize() - 1) {
            Paciente p = modeloListaPacientes.remove(indice);
            modeloListaPacientes.add(indice + 1, p);
            listaManualPacientes.setSelectedIndex(indice + 1);
        }
    }

    /**
     * Ação do botão "Iniciar Simulação"
     */
    private void iniciarSimulacao(ActionEvent e) {
        // 1. Coleta configurações gerais
        AlgoritmoEscalonamento algoritmo = (AlgoritmoEscalonamento) comboAlgoritmo.getSelectedItem();
        int quantum = (Integer) spinnerQuantum.getValue();
        CenarioSimulacao cenario = (CenarioSimulacao) comboCenario.getSelectedItem();

        int numMedicos;
        List<Paciente> pacientes;

        if (cenario == CenarioSimulacao.CUSTOMIZADO) {
            // Se for customizado, usa a lógica antiga
            numMedicos = (Integer) spinnerMedicos.getValue();

            if (radioAleatorio.isSelected()) {
                int qtd = (Integer) spinnerQtdPacientesAleatorios.getValue();
                pacientes = GeradorPacientes.gerarListaAleatoria(qtd);
            } else if (radioPredefinido.isSelected()) {
                String cenarioLegado = (String) comboCargaPacientes.getSelectedItem();
                pacientes = GeradorPacientes.getListaPredefinida(cenarioLegado);
            } else if (radioManual.isSelected()) {
                if (modeloListaPacientes.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Adicione pelo menos um paciente.", "Lista Vazia", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                pacientes = Collections.list(modeloListaPacientes.elements());
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma fonte de pacientes.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // Se for um Cenário 1, 2 ou 3, usa os dados do cenário
            numMedicos = cenario.getNumeroMedicos();
            pacientes = GeradorPacientes.getListaCenario(cenario);
        }

        // 2. Cria Configuração
        ConfiguracaoSimulacao config = new ConfiguracaoSimulacao(algoritmo, numMedicos, pacientes, quantum);

        // 3. Passa o CENÁRIO para a TelaSimulacao
        TelaSimulacao telaSimulacao = new TelaSimulacao(numMedicos, cenario);

        // 4. Passa o CENÁRIO para o Simulador
        Simulador simulador = new Simulador(config, telaSimulacao, cenario);

        // 5. Dá ao Simulador uma referência a si mesmo (para os botões)
        telaSimulacao.setSimulador(simulador);

        telaSimulacao.setVisible(true);

        // 6. Executa em background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                simulador.executarSimulacao();
                return null;
            }
        };
        worker.execute();
        this.setVisible(false);
    }
}
