package org.example.escalonamentodepacientes.enums;

import lombok.Getter;

/**
 * Enum para modelar os cenários de simulação propostos no trabalho.
 * Armazena o nome amigável e o número fixo de médicos de cada um.
 */
@Getter
public enum CenarioSimulacao {
    CUSTOMIZADO("Customizado", -1), // -1 indica que o cenário não é fixo
    CENARIO_1_EMERGENCIA("Cenário 1: Emergência Crítica", 1),
    CENARIO_2_PLANTAO_LOTADO("Cenário 2: Plantão Lotado", 2),
    CENARIO_3_HOSPITAL_MODERNO("Cenário 3: Hospital Moderno", 4);

    private final String nome;
    private final int numeroMedicos;

    CenarioSimulacao(String nome, int numeroMedicos) {
        this.nome = nome;
        this.numeroMedicos = numeroMedicos;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}
