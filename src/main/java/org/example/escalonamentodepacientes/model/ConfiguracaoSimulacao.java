package org.example.escalonamentodepacientes.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.escalonamentodepacientes.enums.AlgoritmoEscalonamento;
import java.util.List;

/**
 * Armazena todos os parâmetros de entrada definidos pelo usuário.
 */
@Getter
@Setter
@ToString
public class ConfiguracaoSimulacao {

    private AlgoritmoEscalonamento algoritmo;
    private int numeroMedicos;
    private List<Paciente> pacientes;
    private int quantum; // Apenas para Round-Robin

    //Com quantum
    public ConfiguracaoSimulacao(AlgoritmoEscalonamento algoritmo, int numeroMedicos, List<Paciente> pacientes, int quantum) {
        this.algoritmo = algoritmo;
        this.numeroMedicos = numeroMedicos;
        this.pacientes = pacientes;
        this.quantum = quantum;
    }

    //Sem quantum
    public ConfiguracaoSimulacao(AlgoritmoEscalonamento algoritmo, int numeroMedicos, List<Paciente> pacientes) {
        this.algoritmo = algoritmo;
        this.numeroMedicos = numeroMedicos;
        this.pacientes = pacientes;
    }

}
