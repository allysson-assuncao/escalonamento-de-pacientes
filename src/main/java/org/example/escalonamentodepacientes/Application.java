package org.example.escalonamentodepacientes;

import org.example.escalonamentodepacientes.gui.TelaConfiguracao;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .headless(false)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            TelaConfiguracao tela = new TelaConfiguracao();
            tela.setVisible(true);
        });

    }

}
