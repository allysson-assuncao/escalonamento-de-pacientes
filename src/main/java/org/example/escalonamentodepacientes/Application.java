package org.example.escalonamentodepacientes;

import org.example.escalonamentodepacientes.controller.TelaSimulacao;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        SwingUtilities.invokeLater(() -> {
            TelaSimulacao tela = new TelaSimulacao();
            tela.setVisible(true);
        });

    }

}
