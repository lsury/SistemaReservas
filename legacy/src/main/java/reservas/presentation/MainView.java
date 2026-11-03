package reservas.presentation;

import javax.swing.*;
import java.net.URL;

public class MainView extends JFrame {

    private JTabbedPane tabs;

    public MainView() {

        setTitle("Sistema de Reservas");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabs = new JTabbedPane();

        if (Sesion.getUsuario().getRol().name().equals("ADMINISTRADOR")) {

            // FUNCIONARIOS
            reservas.presentation.funcionarios.Model modelFuncionarios =
                    new reservas.presentation.funcionarios.Model();

            reservas.presentation.funcionarios.View viewFuncionarios =
                    new reservas.presentation.funcionarios.View();

            new reservas.presentation.funcionarios.Controller(
                    modelFuncionarios,
                    viewFuncionarios
            );

            tabs.addTab(
                    "Funcionarios",
                    cargarIcono("/reservas/presentation/icons/funcionarios.png"),
                    viewFuncionarios
            );

            // CATEGORÍAS
            reservas.presentation.categorias.Model modelCategorias =
                    new reservas.presentation.categorias.Model();

            reservas.presentation.categorias.View viewCategorias =
                    new reservas.presentation.categorias.View();

            new reservas.presentation.categorias.Controller(
                    modelCategorias,
                    viewCategorias
            );

            tabs.addTab(
                    "Categorías",
                    cargarIcono("/reservas/presentation/icons/categorias.png"),
                    viewCategorias
            );

            // RECURSOS
            reservas.presentation.recursos.Model modelRecursos =
                    new reservas.presentation.recursos.Model();

            reservas.presentation.recursos.View viewRecursos =
                    new reservas.presentation.recursos.View();

            new reservas.presentation.recursos.Controller(
                    modelRecursos,
                    viewRecursos
            );

            tabs.addTab(
                    "Recursos",
                    cargarIcono("/reservas/presentation/icons/recursos.png"),
                    viewRecursos
            );

            // CALENDARIZACIÓN
            reservas.presentation.calendarizacion.Model modelCalendarizacion =
                    new reservas.presentation.calendarizacion.Model();

            reservas.presentation.calendarizacion.View viewCalendarizacion =
                    new reservas.presentation.calendarizacion.View();

            new reservas.presentation.calendarizacion.Controller(
                    modelCalendarizacion,
                    viewCalendarizacion
            );

            tabs.addTab(
                    "Calendarización",
                    cargarIcono("/reservas/presentation/icons/calendarizacion.png"),
                    viewCalendarizacion
            );

            // ACTIVIDADES
            reservas.presentation.actividades.Model modelActividades =
                    new reservas.presentation.actividades.Model();

            reservas.presentation.actividades.View viewActividades =
                    new reservas.presentation.actividades.View();

            new reservas.presentation.actividades.Controller(
                    modelActividades,
                    viewActividades
            );

            tabs.addTab(
                    "Actividades",
                    cargarIcono("/reservas/presentation/icons/actividades.png"),
                    viewActividades
            );

            // ESTADÍSTICAS
            reservas.presentation.estadisticas.Model modelEstadisticas =
                    new reservas.presentation.estadisticas.Model();

            reservas.presentation.estadisticas.View viewEstadisticas =
                    new reservas.presentation.estadisticas.View();

            new reservas.presentation.estadisticas.Controller(
                    modelEstadisticas,
                    viewEstadisticas
            );

            tabs.addTab(
                    "Estadísticas",
                    cargarIcono("/reservas/presentation/icons/estadisticas.png"),
                    viewEstadisticas
            );

        } else {

            // RESERVAS
            reservas.presentation.reservas.Model modelReservas =
                    new reservas.presentation.reservas.Model();

            reservas.presentation.reservas.View viewReservas =
                    new reservas.presentation.reservas.View();

            new reservas.presentation.reservas.Controller(
                    modelReservas,
                    viewReservas
            );

            tabs.addTab(
                    "Reservas",
                    cargarIcono("/reservas/presentation/icons/reservas.png"),
                    viewReservas
            );

            // CALENDARIZACIÓN
            reservas.presentation.calendarizacion.Model modelCalendarizacion =
                    new reservas.presentation.calendarizacion.Model();

            reservas.presentation.calendarizacion.View viewCalendarizacion =
                    new reservas.presentation.calendarizacion.View();

            new reservas.presentation.calendarizacion.Controller(
                    modelCalendarizacion,
                    viewCalendarizacion
            );

            tabs.addTab(
                    "Calendarización",
                    cargarIcono("/reservas/presentation/icons/calendarizacion.png"),
                    viewCalendarizacion
            );

            // ACTIVIDADES
            reservas.presentation.actividades.Model modelActividades =
                    new reservas.presentation.actividades.Model();

            reservas.presentation.actividades.View viewActividades =
                    new reservas.presentation.actividades.View();

            new reservas.presentation.actividades.Controller(
                    modelActividades,
                    viewActividades
            );

            tabs.addTab(
                    "Actividades",
                    cargarIcono("/reservas/presentation/icons/actividades.png"),
                    viewActividades
            );

            // ESTADÍSTICAS
            reservas.presentation.estadisticas.Model modelEstadisticas =
                    new reservas.presentation.estadisticas.Model();

            reservas.presentation.estadisticas.View viewEstadisticas =
                    new reservas.presentation.estadisticas.View();

            new reservas.presentation.estadisticas.Controller(
                    modelEstadisticas,
                    viewEstadisticas
            );

            tabs.addTab(
                    "Estadísticas",
                    cargarIcono("/reservas/presentation/icons/estadisticas.png"),
                    viewEstadisticas
            );
        }

        add(tabs);
    }

    private ImageIcon cargarIcono(String ruta) {

        URL url = getClass().getResource(ruta);

        if (url != null) {
            return new ImageIcon(url);
        }

        System.out.println(
                "No se encontró el icono: " + ruta
        );

        return null;
    }
}