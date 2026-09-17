package reservas.presentation.recursos;

import reservas.logic.Recurso;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private Recurso current;
    private List<Recurso> recursos;

    public Model() {
        current = new Recurso();
        recursos = new ArrayList<>();
    }

    public Recurso getCurrent() {
        return current;
    }

    public void setCurrent(Recurso current) {
        this.current = current;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }
}