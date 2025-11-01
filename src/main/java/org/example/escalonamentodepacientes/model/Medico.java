package org.example.escalonamentodepacientes.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.escalonamentodepacientes.enums.StatusMedico;

/**
 * Representa um Médico (Núcleo de CPU).
 * Esta é uma classe POJO simples, gerenciada pelo 'Simulador'.
 */

@Builder
@Getter
@Setter
@ToString
public class Medico {

    private int id;
    private StatusMedico status;


}
