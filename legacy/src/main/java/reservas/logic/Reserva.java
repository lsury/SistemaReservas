package reservas.logic;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import reservas.data.LocalDateAdapter;
import reservas.data.LocalTimeAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Reserva {

    @XmlID
    private String id;

    private String actividad;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate fecha;

    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime horaInicio;

    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime horaFin;

    @XmlIDREF
    private Funcionario funcionario;

    @XmlElementWrapper(name = "recursos")
    @XmlElement(name = "recurso")
    @XmlIDREF
    private List<Recurso> recursos;

    private EstadoReserva estado;

    public Reserva() {
        recursos = new ArrayList<>();
        estado = EstadoReserva.ACTIVA;
    }

    public Reserva(String id, String actividad, LocalDate fecha,
                   LocalTime horaInicio, LocalTime horaFin,
                   Funcionario funcionario, List<Recurso> recursos,
                   EstadoReserva estado) {

        this.id = id;
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.funcionario = funcionario;
        this.recursos = recursos;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }
}