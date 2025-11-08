package org.example.escalonamentodepacientes;


import com.formdev.flatlaf.FlatLightLaf;
import org.example.escalonamentodepacientes.gui.TelaConfiguracao;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;
// Importações necessárias para customizar cores e fontes
import java.awt.Color;
import java.awt.Font;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        // 1. Configura o Look and Feel (a base)
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Falha ao inicializar o Look and Feel (FlatLaf). Usando o padrão.");
        }

        // Define um fundo "gelo" (cinza bem claro) para os painéis
        UIManager.put("Panel.background", Color.decode("#F7F7F7"));
        UIManager.put("Frame.background", Color.decode("#F7F7F7")); // Para a janela


        // Define um azul forte para os títulos das bordas
        UIManager.put("TitledBorder.titleColor", Color.decode("#00539C"));

        // Pega a fonte padrão do TitledBorder e a transforma em NEGRITO
        Font boldFont = UIManager.getFont("TitledBorder.font").deriveFont(Font.BOLD);
        UIManager.put("TitledBorder.font", boldFont);

        new SpringApplicationBuilder(Application.class)
                .headless(false)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            TelaConfiguracao tela = new TelaConfiguracao();
            tela.setVisible(true);
        });
    }
}