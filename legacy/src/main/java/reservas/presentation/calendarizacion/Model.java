package reservas.presentation.calendarizacion;

import reservas.logic.Categoria;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Model {

    private LocalDate fecha;
    private Categoria categoria;

    private List<Recurso> recursos;
    private List<Reserva> reservas;

    public Model() {
        fecha = LocalDate.now();
        categoria = null;
        recursos = new ArrayList<>();
        reservas = new ArrayList<>();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }
}