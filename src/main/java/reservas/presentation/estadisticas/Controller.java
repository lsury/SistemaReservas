package reservas.presentation.estadisticas;

import reservas.logic.Categoria;
import reservas.logic.EstadoReserva;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.logic.Service;
import reservas.presentation.reportes.PdfUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.Map;

import java.awt.event.HierarchyEvent;

public class Controller {

    private Model model;
    private View view;

    public Controller(
            Model model,
            View view) {

        this.model = model;
        this.view = view;

        view.getConsultar()
                .addActionListener(
                        e -> consultar()
                );

        view.getPdfRecursos()
                .addActionListener(
                        e -> generarPdfRecursos()
                );

        view.getPdfActividades()
                .addActionListener(
                        e -> generarPdfActividades()
                );
        view.addHierarchyListener(
                e -> {

                    if ((e.getChangeFlags()
                            & HierarchyEvent.SHOWING_CHANGED) != 0
                            && view.isShowing()) {

                        consultar();
                    }
                }
        );
        consultar();
    }

    // =========================================================
    // CONSULTAR
    // =========================================================

    private void consultar() {

        LocalDate fechaInicio =
                view.getFechaInicio()
                        .getDate();

        LocalDate fechaFin =
                view.getFechaFin()
                        .getDate();

        if (!validarPeriodo(
                fechaInicio,
                fechaFin)) {

            return;
        }

        try {

            model.setFechaInicio(
                    fechaInicio
            );

            model.setFechaFin(
                    fechaFin
            );

            Map<String, Integer> recursosPorCategoria =
                    calcularRecursosPorCategoria(
                            fechaInicio,
                            fechaFin
                    );

            Map<String, Integer> actividadesPorSemana =
                    calcularActividadesPorSemana(
                            fechaInicio,
                            fechaFin
                    );

            model.setRecursosPorCategoria(
                    recursosPorCategoria
            );

            model.setActividadesPorSemana(
                    actividadesPorSemana
            );

            cargarTablaRecursos(
                    recursosPorCategoria
            );

            cargarTablaActividades(
                    actividadesPorSemana
            );

            cargarGraficoRecursos(
                    recursosPorCategoria
            );

            cargarGraficoActividades(
                    actividadesPorSemana
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible calcular las estadísticas.\n"
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // VALIDACIÓN DEL PERÍODO
    // =========================================================

    private boolean validarPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (fechaInicio == null
                || fechaFin == null) {

            mostrarError(
                    "Debe seleccionar fecha de inicio y fecha de fin."
            );

            return false;
        }

        if (fechaInicio.isAfter(
                fechaFin)) {

            mostrarError(
                    "La fecha de inicio no puede ser posterior "
                            + "a la fecha de fin."
            );

            return false;
        }

        return true;
    }

    // =========================================================
    // RECURSOS RESERVADOS POR CATEGORÍA
    // =========================================================

    private Map<String, Integer>
    calcularRecursosPorCategoria(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        Map<String, Integer> resultado =
                new LinkedHashMap<>();

        for (Categoria categoria :
                Service.instance()
                        .getCategorias()) {

            if (categoria == null
                    || categoria.getDescripcion() == null) {

                continue;
            }

            resultado.put(
                    categoria.getDescripcion(),
                    0
            );
        }

        for (Reserva reserva :
                Service.instance()
                        .getReservas()) {

            if (reserva == null) {
                continue;
            }

            if (!reservaDentroPeriodo(
                    reserva,
                    fechaInicio,
                    fechaFin)) {

                continue;
            }

            if (reserva.getEstado()
                    != EstadoReserva.ACTIVA) {

                continue;
            }

            if (reserva.getRecursos() == null) {
                continue;
            }

            for (Recurso recurso :
                    reserva.getRecursos()) {

                if (recurso == null) {
                    continue;
                }

                if (recurso.getCategoria() == null) {
                    continue;
                }

                if (recurso.getCategoria()
                        .getDescripcion() == null) {

                    continue;
                }

                String categoria =
                        recurso.getCategoria()
                                .getDescripcion();

                resultado.put(
                        categoria,
                        resultado.getOrDefault(
                                categoria,
                                0
                        ) + 1
                );
            }
        }

        return resultado;
    }

    // =========================================================
    // ACTIVIDADES POR SEMANA
    // =========================================================

    private Map<String, Integer>
    calcularActividadesPorSemana(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        Map<String, Integer> resultado =
                new LinkedHashMap<>();

        /*
         * Utilizamos semanas ISO para mantener
         * el mismo criterio lunes-domingo usado
         * en el módulo de Actividades.
         */
        WeekFields weekFields =
                WeekFields.ISO;

        for (Reserva reserva :
                Service.instance()
                        .getReservas()) {

            if (reserva == null) {
                continue;
            }

            if (!reservaDentroPeriodo(
                    reserva,
                    fechaInicio,
                    fechaFin)) {

                continue;
            }

            if (reserva.getEstado()
                    != EstadoReserva.ACTIVA) {

                continue;
            }

            LocalDate fecha =
                    reserva.getFecha();

            int semana =
                    fecha.get(
                            weekFields.weekOfWeekBasedYear()
                    );

            int anio =
                    fecha.get(
                            weekFields.weekBasedYear()
                    );

            String clave =
                    "Semana "
                            + semana
                            + " - "
                            + anio;

            resultado.put(
                    clave,
                    resultado.getOrDefault(
                            clave,
                            0
                    ) + 1
            );
        }

        return resultado;
    }

    // =========================================================
    // VALIDAR SI RESERVA ESTÁ DENTRO DEL PERÍODO
    // =========================================================

    private boolean reservaDentroPeriodo(
            Reserva reserva,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (reserva == null
                || reserva.getFecha() == null
                || fechaInicio == null
                || fechaFin == null) {

            return false;
        }

        return !reserva.getFecha()
                .isBefore(fechaInicio)
                &&
                !reserva.getFecha()
                        .isAfter(fechaFin);
    }

    // =========================================================
    // TABLA RECURSOS
    // =========================================================

    private void cargarTablaRecursos(
            Map<String, Integer> datos) {

        DefaultTableModel tablaModel =
                new DefaultTableModel(
                        new Object[]{
                                "Categoría",
                                "Cantidad"
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

        if (datos != null) {

            for (Map.Entry<String, Integer> entry :
                    datos.entrySet()) {

                tablaModel.addRow(
                        new Object[]{
                                entry.getKey(),
                                entry.getValue()
                        }
                );
            }
        }

        view.getTablaRecursos()
                .setModel(
                        tablaModel
                );
    }

    // =========================================================
    // TABLA ACTIVIDADES
    // =========================================================

    private void cargarTablaActividades(
            Map<String, Integer> datos) {

        DefaultTableModel tablaModel =
                new DefaultTableModel(
                        new Object[]{
                                "Semana",
                                "Cantidad"
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

        if (datos != null) {

            for (Map.Entry<String, Integer> entry :
                    datos.entrySet()) {

                tablaModel.addRow(
                        new Object[]{
                                entry.getKey(),
                                entry.getValue()
                        }
                );
            }
        }

        view.getTablaActividades()
                .setModel(
                        tablaModel
                );
    }

    // =========================================================
    // GRÁFICO RECURSOS
    // =========================================================

    private void cargarGraficoRecursos(
            Map<String, Integer> datos) {

        JPanel panel =
                view.getGraficoRecursos();

        panel.removeAll();

        panel.setLayout(
                new BorderLayout()
        );

        panel.add(
                new GraficoBarras(
                        datos,
                        "Cantidad de recursos reservados"
                ),
                BorderLayout.CENTER
        );

        panel.revalidate();
        panel.repaint();
    }

    // =========================================================
    // GRÁFICO ACTIVIDADES
    // =========================================================

    private void cargarGraficoActividades(
            Map<String, Integer> datos) {

        JPanel panel =
                view.getGraficoActividades();

        panel.removeAll();

        panel.setLayout(
                new BorderLayout()
        );

        panel.add(
                new GraficoBarras(
                        datos,
                        "Cantidad de actividades"
                ),
                BorderLayout.CENTER
        );

        panel.revalidate();
        panel.repaint();
    }

    // =========================================================
    // PANEL PARA GRÁFICO DE BARRAS
    // =========================================================

    private static class GraficoBarras
            extends JPanel {

        private final Map<String, Integer> datos;
        private final String titulo;

        public GraficoBarras(
                Map<String, Integer> datos,
                String titulo) {

            this.datos = datos;
            this.titulo = titulo;

            setPreferredSize(
                    new Dimension(
                            400,
                            250
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g;

            int ancho =
                    getWidth();

            int alto =
                    getHeight();

            g2.drawString(
                    titulo,
                    15,
                    20
            );

            if (datos == null
                    || datos.isEmpty()) {

                g2.drawString(
                        "No hay datos para mostrar",
                        15,
                        50
                );

                return;
            }

            int margenIzquierdo = 45;
            int margenInferior = 55;
            int margenSuperior = 35;

            int altoGrafico =
                    alto
                            - margenInferior
                            - margenSuperior;

            int anchoGrafico =
                    ancho
                            - margenIzquierdo
                            - 20;

            if (altoGrafico <= 0
                    || anchoGrafico <= 0) {

                return;
            }

            int maximo = 1;

            for (Integer valor :
                    datos.values()) {

                if (valor != null
                        && valor > maximo) {

                    maximo = valor;
                }
            }

            g2.drawLine(
                    margenIzquierdo,
                    margenSuperior,
                    margenIzquierdo,
                    margenSuperior
                            + altoGrafico
            );

            g2.drawLine(
                    margenIzquierdo,
                    margenSuperior
                            + altoGrafico,
                    margenIzquierdo
                            + anchoGrafico,
                    margenSuperior
                            + altoGrafico
            );

            int cantidad =
                    datos.size();

            int anchoDisponible =
                    anchoGrafico
                            / Math.max(
                            cantidad,
                            1
                    );

            int anchoBarra =
                    Math.max(
                            5,
                            anchoDisponible / 2
                    );

            int indice = 0;

            for (Map.Entry<String, Integer> entry :
                    datos.entrySet()) {

                int valor =
                        entry.getValue() != null
                                ? entry.getValue()
                                : 0;

                int alturaBarra =
                        (int) (
                                (
                                        (double) valor
                                                / maximo
                                )
                                        * (
                                        altoGrafico
                                                - 20
                                )
                        );

                int x =
                        margenIzquierdo
                                + indice
                                * anchoDisponible
                                + (
                                anchoDisponible
                                        - anchoBarra
                        ) / 2;

                int y =
                        margenSuperior
                                + altoGrafico
                                - alturaBarra;

                g2.fillRect(
                        x,
                        y,
                        anchoBarra,
                        alturaBarra
                );

                g2.drawString(
                        String.valueOf(valor),
                        x,
                        Math.max(
                                margenSuperior,
                                y - 5
                        )
                );

                String etiqueta =
                        entry.getKey() != null
                                ? entry.getKey()
                                : "";

                if (etiqueta.length() > 12) {

                    etiqueta =
                            etiqueta.substring(
                                    0,
                                    12
                            )
                                    + "...";
                }

                g2.drawString(
                        etiqueta,
                        x,
                        margenSuperior
                                + altoGrafico
                                + 18
                );

                indice++;
            }
        }
    }

    // =========================================================
    // PDF RECURSOS
    // =========================================================

    private void generarPdfRecursos() {

        LocalDate inicio =
                view.getFechaInicio()
                        .getDate();

        LocalDate fin =
                view.getFechaFin()
                        .getDate();

        if (!validarPeriodo(
                inicio,
                fin)) {

            return;
        }

        try {

            Map<String, Integer> datosCalculados =
                    calcularRecursosPorCategoria(
                            inicio,
                            fin
                    );

            cargarTablaRecursos(
                    datosCalculados
            );

            cargarGraficoRecursos(
                    datosCalculados
            );

            JTable tabla =
                    view.getTablaRecursos();

            if (tabla.getRowCount() == 0) {

                JOptionPane.showMessageDialog(
                        view,
                        "No hay datos de recursos para generar el PDF.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            String[] columnas =
                    obtenerColumnas(
                            tabla
                    );

            Object[][] datos =
                    obtenerDatosTabla(
                            tabla
                    );

            String titulo =
                    "Recursos Reservados por Categoría\n"
                            + "Período: "
                            + inicio
                            + " al "
                            + fin;

            PdfUtil.generarReporte(
                    titulo,
                    columnas,
                    datos,
                    "estadisticas_recursos.pdf"
            );

            JOptionPane.showMessageDialog(
                    view,
                    "PDF de recursos generado correctamente.",
                    "Reporte generado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible generar el PDF de recursos.\n"
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // PDF ACTIVIDADES
    // =========================================================

    private void generarPdfActividades() {

        LocalDate inicio =
                view.getFechaInicio()
                        .getDate();

        LocalDate fin =
                view.getFechaFin()
                        .getDate();

        if (!validarPeriodo(
                inicio,
                fin)) {

            return;
        }

        try {

            Map<String, Integer> datosCalculados =
                    calcularActividadesPorSemana(
                            inicio,
                            fin
                    );

            cargarTablaActividades(
                    datosCalculados
            );

            cargarGraficoActividades(
                    datosCalculados
            );

            JTable tabla =
                    view.getTablaActividades();

            if (tabla.getRowCount() == 0) {

                JOptionPane.showMessageDialog(
                        view,
                        "No hay datos de actividades para generar el PDF.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            String[] columnas =
                    obtenerColumnas(
                            tabla
                    );

            Object[][] datos =
                    obtenerDatosTabla(
                            tabla
                    );

            String titulo =
                    "Actividades por Semana\n"
                            + "Período: "
                            + inicio
                            + " al "
                            + fin;

            PdfUtil.generarReporte(
                    titulo,
                    columnas,
                    datos,
                    "estadisticas_actividades.pdf"
            );

            JOptionPane.showMessageDialog(
                    view,
                    "PDF de actividades generado correctamente.",
                    "Reporte generado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible generar el PDF de actividades.\n"
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // UTILIDADES PARA PDF
    // =========================================================

    private String[] obtenerColumnas(
            JTable tabla) {

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

        return columnas;
    }

    private Object[][] obtenerDatosTabla(
            JTable tabla) {

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

        return datos;
    }

    // =========================================================
    // MENSAJES
    // =========================================================

    private void mostrarError(
            String mensaje) {

        JOptionPane.showMessageDialog(
                view,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}