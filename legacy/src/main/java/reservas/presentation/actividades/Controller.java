package reservas.presentation.actividades;

import reservas.logic.EstadoReserva;
import reservas.logic.Reserva;
import reservas.logic.Service;
import reservas.presentation.reportes.PdfUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.HierarchyEvent;


public class Controller {

    private Model model;
    private View view;

    public Controller(Model model, View view) {

        this.model = model;
        this.view = view;

        view.getConsultar()
                .addActionListener(
                        e -> consultar()
                );

        view.getPdf()
                .addActionListener(
                        e -> generarPdf()
                );

        consultar();

        view.addHierarchyListener(
                e -> {

                    if ((e.getChangeFlags()
                            & HierarchyEvent.SHOWING_CHANGED) != 0
                            && view.isShowing()) {

                        consultar();
                    }
                }
        );
    }

    private void consultar() {

        LocalDate fechaSeleccionada =
                view.getSemana()
                        .getDate();

        if (fechaSeleccionada == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "Debe seleccionar una fecha",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        try {

            LocalDate lunes =
                    fechaSeleccionada.with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.MONDAY
                            )
                    );

            model.setInicioSemana(
                    lunes
            );

            List<Reserva> reservasSemana =
                    obtenerReservasSemana(
                            lunes
                    );

            model.setReservas(
                    reservasSemana
            );

            cargarTabla(
                    lunes,
                    reservasSemana
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    "No fue posible consultar las actividades.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private List<Reserva> obtenerReservasSemana(
            LocalDate lunes) {

        List<Reserva> resultado =
                new ArrayList<>();

        if (lunes == null) {
            return resultado;
        }

        LocalDate domingo =
                lunes.plusDays(6);

        for (Reserva reserva :
                Service.instance()
                        .getReservas()) {

            if (reserva == null) {
                continue;
            }

            if (reserva.getFecha() == null) {
                continue;
            }

            if (reserva.getEstado()
                    != EstadoReserva.ACTIVA) {

                continue;
            }

            LocalDate fechaReserva =
                    reserva.getFecha();

            boolean dentroSemana =
                    !fechaReserva.isBefore(
                            lunes
                    )
                            &&
                            !fechaReserva.isAfter(
                                    domingo
                            );

            if (dentroSemana) {

                resultado.add(
                        reserva
                );
            }
        }

        return resultado;
    }

    private void cargarTabla(
            LocalDate lunes,
            List<Reserva> reservas) {

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern(
                        "dd/MM"
                );

        String[] columnas =
                new String[]{
                        "Hora",

                        "Lunes "
                                + lunes.format(
                                formato
                        ),

                        "Martes "
                                + lunes.plusDays(1)
                                .format(
                                        formato
                                ),

                        "Miércoles "
                                + lunes.plusDays(2)
                                .format(
                                        formato
                                ),

                        "Jueves "
                                + lunes.plusDays(3)
                                .format(
                                        formato
                                ),

                        "Viernes "
                                + lunes.plusDays(4)
                                .format(
                                        formato
                                ),

                        "Sábado "
                                + lunes.plusDays(5)
                                .format(
                                        formato
                                ),

                        "Domingo "
                                + lunes.plusDays(6)
                                .format(
                                        formato
                                )
                };

        DefaultTableModel tablaModel =
                new DefaultTableModel(
                        columnas,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        for (int hora = 6;
             hora <= 22;
             hora++) {

            Object[] fila =
                    new Object[8];

            LocalTime horaFila =
                    LocalTime.of(
                            hora,
                            0
                    );

            fila[0] =
                    String.format(
                            "%02d:00",
                            hora
                    );

            for (int dia = 0;
                 dia < 7;
                 dia++) {

                LocalDate fechaDia =
                        lunes.plusDays(
                                dia
                        );

                List<Reserva> reservasCelda =
                        buscarReservas(
                                fechaDia,
                                horaFila,
                                reservas
                        );

                if (reservasCelda.isEmpty()) {

                    fila[dia + 1] =
                            "";

                } else {

                    StringBuilder texto =
                            new StringBuilder();

                    for (Reserva reserva :
                            reservasCelda) {

                        if (texto.length() > 0) {

                            texto.append(
                                    " | "
                            );
                        }

                        String actividad =
                                reserva.getActividad();

                        if (actividad != null) {

                            texto.append(
                                    actividad
                            );
                        }

                        if (reserva.getFuncionario()
                                != null) {

                            texto.append(
                                    " - "
                            );

                            if (reserva.getFuncionario()
                                    .getNombre()
                                    != null
                                    &&
                                    !reserva.getFuncionario()
                                            .getNombre()
                                            .isBlank()) {

                                texto.append(
                                        reserva.getFuncionario()
                                                .getNombre()
                                );

                            } else {

                                texto.append(
                                        reserva.getFuncionario()
                                                .getId()
                                );
                            }
                        }
                    }

                    fila[dia + 1] =
                            texto.toString();
                }
            }

            tablaModel.addRow(
                    fila
            );
        }

        view.getTabla()
                .setModel(
                        tablaModel
                );

        configurarColumnas();
    }

    private List<Reserva> buscarReservas(
            LocalDate fecha,
            LocalTime hora,
            List<Reserva> reservas) {

        List<Reserva> resultado =
                new ArrayList<>();

        if (fecha == null
                || hora == null
                || reservas == null) {

            return resultado;
        }

        for (Reserva reserva :
                reservas) {

            if (reserva == null) {
                continue;
            }

            if (reserva.getFecha() == null) {
                continue;
            }

            if (!reserva.getFecha()
                    .equals(
                            fecha
                    )) {

                continue;
            }

            if (reserva.getHoraInicio() == null
                    || reserva.getHoraFin() == null) {

                continue;
            }

            boolean dentroHorario =
                    !hora.isBefore(
                            reserva.getHoraInicio()
                    )
                            &&
                            hora.isBefore(
                                    reserva.getHoraFin()
                            );

            if (dentroHorario) {

                resultado.add(
                        reserva
                );
            }
        }

        return resultado;
    }

    private void configurarColumnas() {

        if (view.getTabla()
                .getColumnCount() == 0) {

            return;
        }

        view.getTabla()
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(
                        70
                );

        for (int i = 1;
             i < view.getTabla()
                     .getColumnCount();
             i++) {

            view.getTabla()
                    .getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(
                            220
                    );
        }
    }

    private void generarPdf() {

        LocalDate fechaSeleccionada =
                view.getSemana()
                        .getDate();

        if (fechaSeleccionada == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "Debe seleccionar una fecha.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        try {

            LocalDate lunes =
                    fechaSeleccionada.with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.MONDAY
                            )
                    );

            LocalDate domingo =
                    lunes.plusDays(
                            6
                    );

            List<Reserva> reservasSemana =
                    obtenerReservasSemana(
                            lunes
                    );

            cargarTabla(
                    lunes,
                    reservasSemana
            );

            JTable tabla =
                    view.getTabla();

            if (tabla.getColumnCount() <= 1) {

                JOptionPane.showMessageDialog(
                        view,
                        "Primero debe consultar una semana.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            String[] columnas =
                    new String[
                            tabla.getColumnCount()
                            ];

            for (int columna = 0;
                 columna < tabla.getColumnCount();
                 columna++) {

                columnas[columna] =
                        tabla.getColumnName(
                                columna
                        );
            }

            Object[][] datos =
                    new Object[
                            tabla.getRowCount()
                            ][
                            tabla.getColumnCount()
                            ];

            for (int fila = 0;
                 fila < tabla.getRowCount();
                 fila++) {

                for (int columna = 0;
                     columna < tabla.getColumnCount();
                     columna++) {

                    datos[fila][columna] =
                            tabla.getValueAt(
                                    fila,
                                    columna
                            );
                }
            }

            String titulo =
                    "Reporte de Actividades\n"
                            + "Semana del "
                            + lunes
                            + " al "
                            + domingo;

            PdfUtil.generarReporte(
                    titulo,
                    columnas,
                    datos,
                    "actividades.pdf"
            );

            JOptionPane.showMessageDialog(
                    view,
                    "PDF generado correctamente.",
                    "Reporte generado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    "No fue posible generar el PDF.\n"
                            + e.getMessage(),
                    "Error al generar PDF",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}