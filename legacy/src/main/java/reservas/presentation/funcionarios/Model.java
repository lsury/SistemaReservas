package reservas.presentation.funcionarios;

import reservas.logic.Funcionario;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private Funcionario current;
    private List<Funcionario> funcionarios;

    public Model() {
        current = new Funcionario();
        funcionarios = new ArrayList<>();
    }

    public Funcionario getCurrent() {
        return current;
    }

    public void setCurrent(Funcionario current) {
        this.current = current;
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }
}