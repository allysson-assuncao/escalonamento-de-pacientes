package org.example.escalonamentodepacientes.simulacao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.escalonamentodepacientes.enums.StatusPaciente;
import org.example.escalonamentodepacientes.model.ConfiguracaoSimulacao;
import org.example.escalonamentodepacientes.model.Paciente;
import java.util.List;


@Getter
@Setter
@ToString
public class Simulador {

    private ConfiguracaoSimulacao configuracao;
    private long tempoGlobal;

    private List<Paciente> filaDePacientes;

    public Simulador(ConfiguracaoSimulacao configuracao, List<Paciente> filaDePacientes) {
        this.configuracao = configuracao;
        this.tempoGlobal = 0;
        this.filaDePacientes = filaDePacientes;
    }

    public void iniciarSimulacao(){

        while(filaDePacientes.isEmpty()){



            for(int i = 0; i < this.configuracao.getNumeroMedicos(); i++){

                // Thread ...



            }

            this.tempoGlobal += 1000; // Um segundo na simulação
            for(Paciente p : filaDePacientes){
                 p.atualizaPaciente();
                 if(p.getStatus() == StatusPaciente.CONCLUIDO) filaDePacientes.remove(p);
            }

        }
    }

}


