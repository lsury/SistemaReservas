package reservas.presentation.reservas;

import reservas.logic.Categoria;
import reservas.logic.Reserva;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private Reserva current;
    private List<Reserva> reservas;
    private List<Categoria> categorias;

    public Model() {
        current = new Reserva();
        reservas = new ArrayList<>();
        categorias = new ArrayList<>();
    }

    public Reserva getCurrent() {
        return current;
    }

    public void setCurrent(Reserva current) {
        this.current = current;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }
}