package reservas.presentation.estadisticas;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Model {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Map<String, Integer> recursosPorCategoria;
    private Map<String, Integer> actividadesPorSemana;

    public Model() {

        fechaInicio = LocalDate.now();
        fechaFin = LocalDate.now();

        recursosPorCategoria =
                new LinkedHashMap<>();

        actividadesPorSemana =
                new LinkedHashMap<>();
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(
            LocalDate fechaInicio) {

        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(
            LocalDate fechaFin) {

        this.fechaFin = fechaFin;
    }

    public Map<String, Integer>
    getRecursosPorCategoria() {

        return recursosPorCategoria;
    }

    public void setRecursosPorCategoria(
            Map<String, Integer>
                    recursosPorCategoria) {

        this.recursosPorCategoria =
                recursosPorCategoria;
    }

    public Map<String, Integer>
    getActividadesPorSemana() {

        return actividadesPorSemana;
    }

    public void setActividadesPorSemana(
            Map<String, Integer>
                    actividadesPorSemana) {

        this.actividadesPorSemana =
                actividadesPorSemana;
    }
}