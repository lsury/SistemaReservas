package reservas.presentation.actividades;

import reservas.logic.Reserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Model {

    private LocalDate inicioSemana;
    private List<Reserva> reservas;

    public Model() {
        inicioSemana = LocalDate.now();
        reservas = new ArrayList<>();
    }

    public LocalDate getInicioSemana() {
        return inicioSemana;
    }

    public void setInicioSemana(LocalDate inicioSemana) {
        this.inicioSemana = inicioSemana;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }
}