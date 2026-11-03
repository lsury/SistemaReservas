package reservas.presentation.categorias;

import reservas.logic.Categoria;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private Categoria current;
    private List<Categoria> categorias;

    public Model() {
        current = new Categoria();
        categorias = new ArrayList<>();
    }

    public Categoria getCurrent() {
        return current;
    }

    public void setCurrent(Categoria current) {
        this.current = current;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }
}