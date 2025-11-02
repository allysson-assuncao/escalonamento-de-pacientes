package org.example.escalonamentodepacientes.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.escalonamentodepacientes.enums.StatusMedico;
import org.example.escalonamentodepacientes.enums.StatusPaciente;

/**
 * Representa um Médico (Núcleo de CPU).
 */

@Getter
@Setter
@ToString
public class Medico {

    private int id;
    private StatusMedico status;
    private Paciente pacienteAtual;
    private int tempoOcupado;

    public Medico(int id) {
        this.id = id;
        this.status = StatusMedico.OCIOSO;
        this.tempoOcupado = 0;
    }

    public boolean estaOcioso() {
        return this.status == StatusMedico.OCIOSO;
    }

    ;

    /**
     * Atribui um paciente a este médico, iniciando o atendimento.
     */
    public void atenderPaciente(Paciente paciente) {
        this.pacienteAtual = paciente;
        this.status = StatusMedico.OCUPADO;
        paciente.setStatus(StatusPaciente.EXECUTANDO);
    }

    /**
     * Libera o médico, tornando-o ocioso.
     *
     * @return O paciente que acabou de ser atendido.
     */
    public Paciente liberarMedico() {
        Paciente pacienteConcluido = this.pacienteAtual;
        this.pacienteAtual = null;
        this.status = StatusMedico.OCIOSO;

        // Retorno usado quando o paciente precisa ser movido (preempção)
        return pacienteConcluido;
    }

    /**
     * Processa o trabalho do médico neste "tick" do relógio.
     */
    public void executaTickConsulta() {
        this.tempoOcupado++;

        // Lógica de incremento interna ao paciente
        this.pacienteAtual.executarTick();
    }

}
