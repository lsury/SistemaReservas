package reservas.presentation.calendarizacion;

import reservas.logic.Categoria;
import reservas.logic.EstadoReserva;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.logic.Service;
import reservas.presentation.reportes.PdfUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.HierarchyEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private Model model;
    private View view;

    private boolean actualizandoCategorias = false;

    public Controller(
            Model model,
            View view) {

        this.model = model;
        this.view = view;

        cargarCategorias();

        view.getConsultar()
                .addActionListener(
                        e -> consultar()
                );

        view.getCategoria()
                .addActionListener(
                        e -> {

                            if (!actualizandoCategorias) {
                                consultar();
                            }
                        }
                );

        view.getPdf()
                .addActionListener(
                        e -> generarPdf()
                );

        /*
         * Cuando se abre el combo se vuelven
         * a consultar las categorías actuales.
         */
        view.getCategoria()
                .addPopupMenuListener(
                        new PopupMenuListener() {

                            @Override
                            public void popupMenuWillBecomeVisible(
                                    PopupMenuEvent e) {

                                cargarCategorias();
                            }

                            @Override
                            public void popupMenuWillBecomeInvisible(
                                    PopupMenuEvent e) {
                            }

                            @Override
                            public void popupMenuCanceled(
                                    PopupMenuEvent e) {
                            }
                        }
                );

        /*
         * Cuando la pestaña de Calendarización
         * vuelve a mostrarse se refrescan:
         *
         * - categorías
         * - recursos
         * - reservas
         * - tabla
         */
        view.addHierarchyListener(
                e -> {

                    if ((e.getChangeFlags()
                            & HierarchyEvent.SHOWING_CHANGED) != 0
                            && view.isShowing()) {

                        cargarCategorias();
                        consultar();
                    }
                }
        );

        consultar();
    }

    // =========================================================
    // CARGAR CATEGORÍAS
    // =========================================================

    private void cargarCategorias() {

        actualizandoCategorias = true;

        try {

            Categoria seleccionada =
                    (Categoria) view.getCategoria()
                            .getSelectedItem();

            String idSeleccionado =
                    seleccionada != null
                            ? seleccionada.getId()
                            : null;

            view.getCategoria()
                    .removeAllItems();

            List<Categoria> categorias =
                    Service.instance()
                            .getCategorias();

            for (Categoria categoria :
                    categorias) {

                view.getCategoria()
                        .addItem(
                                categoria
                        );
            }

            /*
             * Intentamos conservar la categoría
             * que estaba seleccionada.
             */
            if (idSeleccionado != null) {

                for (int i = 0;
                     i < view.getCategoria()
                             .getItemCount();
                     i++) {

                    Categoria categoria =
                            view.getCategoria()
                                    .getItemAt(i);

                    if (categoria.getId()
                            .equals(idSeleccionado)) {

                        view.getCategoria()
                                .setSelectedIndex(i);

                        return;
                    }
                }
            }

            /*
             * Si la anterior ya no existe,
             * seleccionamos la primera.
             */
            if (view.getCategoria()
                    .getItemCount() > 0) {

                view.getCategoria()
                        .setSelectedIndex(0);
            }

        } finally {

            actualizandoCategorias = false;
        }
    }

    // =========================================================
    // CONSULTAR
    // =========================================================

    private void consultar() {

        LocalDate fecha =
                view.getFecha()
                        .getDate();

        Categoria categoria =
                (Categoria) view.getCategoria()
                        .getSelectedItem();

        if (fecha == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "Debe seleccionar una fecha",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (categoria == null) {

            cargarTablaVacia();

            return;
        }

        try {

            model.setFecha(
                    fecha
            );

            model.setCategoria(
                    categoria
            );

            /*
             * IMPORTANTE:
             *
             * Esto consulta nuevamente Service,
             * por lo que si se creó un recurso
             * nuevo aparecerá inmediatamente.
             */
            List<Recurso> recursos =
                    Service.instance()
                            .buscarRecursosPorCategoria(
                                    categoria
                            );

            model.setRecursos(
                    recursos
            );

            /*
             * También se vuelven a obtener
             * las reservas actuales.
             */
            List<Reserva> reservasFecha =
                    obtenerReservasFecha(
                            fecha
                    );

            model.setReservas(
                    reservasFecha
            );

            cargarTabla(
                    recursos,
                    reservasFecha
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    "No fue posible consultar la calendarización.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            cargarTablaVacia();
        }
    }

    // =========================================================
    // OBTENER RESERVAS DE UNA FECHA
    // =========================================================

    private List<Reserva> obtenerReservasFecha(
            LocalDate fecha) {

        List<Reserva> resultado =
                new ArrayList<>();

        if (fecha == null) {
            return resultado;
        }

        for (Reserva reserva :
                Service.instance()
                        .getReservas()) {

            if (reserva == null) {
                continue;
            }

            if (reserva.getFecha() == null) {
                continue;
            }

            if (!reserva.getFecha()
                    .equals(fecha)) {

                continue;
            }

            /*
             * Las canceladas no ocupan recursos.
             */
            if (reserva.getEstado()
                    != EstadoReserva.ACTIVA) {

                continue;
            }

            resultado.add(
                    reserva
            );
        }

        return resultado;
    }

    // =========================================================
    // CARGAR TABLA
    // =========================================================

    private void cargarTabla(
            List<Recurso> recursos,
            List<Reserva> reservas) {

        String[] columnas =
                new String[
                        recursos.size() + 1
                        ];

        columnas[0] =
                "Hora";

        for (int i = 0;
             i < recursos.size();
             i++) {

            columnas[i + 1] =
                    recursos.get(i)
                            .getId();
        }

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
                    new Object[
                            recursos.size() + 1
                            ];

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

            for (int i = 0;
                 i < recursos.size();
                 i++) {

                Recurso recurso =
                        recursos.get(i);

                Reserva reserva =
                        buscarReserva(
                                recurso,
                                horaFila,
                                reservas
                        );

                if (reserva == null) {

                    fila[i + 1] =
                            "Libre";

                } else {

                    String funcionario =
                            "";

                    if (reserva.getFuncionario() != null) {

                        String nombre =
                                reserva.getFuncionario()
                                        .getNombre();

                        if (nombre != null
                                && !nombre.isBlank()) {

                            funcionario =
                                    nombre;

                        } else {

                            funcionario =
                                    reserva.getFuncionario()
                                            .getId();
                        }
                    }

                    fila[i + 1] =
                            reserva.getId()
                                    + " - "
                                    + reserva.getActividad()
                                    + " - "
                                    + funcionario;
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

        view.getTabla()
                .revalidate();

        view.getTabla()
                .repaint();
    }

    // =========================================================
    // BUSCAR RESERVA
    // =========================================================

    private Reserva buscarReserva(
            Recurso recurso,
            LocalTime hora,
            List<Reserva> reservas) {

        if (recurso == null
                || hora == null
                || reservas == null) {

            return null;
        }

        for (Reserva reserva :
                reservas) {

            if (reserva == null) {
                continue;
            }

            if (!usaRecurso(
                    reserva,
                    recurso)) {

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

                return reserva;
            }
        }

        return null;
    }

    // =========================================================
    // VERIFICAR RECURSO
    // =========================================================

    private boolean usaRecurso(
            Reserva reserva,
            Recurso recurso) {

        if (reserva == null
                || recurso == null
                || recurso.getId() == null) {

            return false;
        }

        if (reserva.getRecursos() == null) {
            return false;
        }

        for (Recurso recursoReserva :
                reserva.getRecursos()) {

            if (recursoReserva == null
                    || recursoReserva.getId() == null) {

                continue;
            }

            if (recursoReserva.getId()
                    .equalsIgnoreCase(
                            recurso.getId()
                    )) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // CONFIGURAR COLUMNAS
    // =========================================================

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
                            230
                    );
        }
    }

    // =========================================================
    // TABLA VACÍA
    // =========================================================

    private void cargarTablaVacia() {

        DefaultTableModel tablaModel =
                new DefaultTableModel(
                        new Object[]{
                                "Hora"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        view.getTabla()
                .setModel(
                        tablaModel
                );
    }

    // =========================================================
    // GENERAR PDF
    // =========================================================

    private void generarPdf() {

        /*
         * Refrescamos antes de generar el reporte.
         */
        consultar();

        JTable tabla =
                view.getTabla();

        if (tabla.getColumnCount() <= 1) {

            JOptionPane.showMessageDialog(
                    view,
                    "Primero debe consultar una categoría con recursos.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        LocalDate fecha =
                view.getFecha()
                        .getDate();

        Categoria categoria =
                (Categoria) view.getCategoria()
                        .getSelectedItem();

        if (fecha == null
                || categoria == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "Debe seleccionar fecha y categoría.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
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
                "Calendarización de Recursos\n"
                        + "Fecha: "
                        + fecha
                        + "\nCategoría: "
                        + categoria.getDescripcion();

        try {

            PdfUtil.generarReporte(
                    titulo,
                    columnas,
                    datos,
                    "calendarizacion.pdf"
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